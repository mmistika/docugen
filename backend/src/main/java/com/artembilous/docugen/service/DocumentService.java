package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.DocumentDTO;
import com.artembilous.docugen.dto.GenerateDocumentRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.DocumentRepository;
import com.artembilous.docugen.repository.TemplateRepository;
import com.artembilous.docugen.repository.TemplateVersionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final PdfService pdfService;
    private final AuditService auditService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Matches {word} placeholder
    private static final Pattern LEFTOVER_PLACEHOLDER = Pattern.compile("\\{[^}]+}");

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'document:generate')")
    public byte[] generate(User user, Long orgId, GenerateDocumentRequest req) {

        Template template = templateRepository
                .findByTemplateIdAndOrganisation_OrganisationId(req.templateId(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found"));

        TemplateVersion version = versionRepository
                .findLatest(req.templateId())
                .orElseThrow(() -> new IllegalStateException("No template version"));

        JsonNode manifest = parse(version.getManifest());
        validate(manifest, req.data());

        String html = render(version.getContent(), req.data());

        byte[] pdf = pdfService.generateFromHtml(html);

        if (!version.getStatus().equals(TemplateVersionStatus.DRAFT)) {
            Document doc = new Document();
            doc.setOrganisation(template.getOrganisation());
            doc.setTemplateVersion(version);
            doc.setName(resolveName(req.name(), template.getName()));
            doc.setStatus(DocumentStatus.DRAFT);
            doc.setData(toJson(req.data()));
            doc.setFile(pdf);
            documentRepository.save(doc);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("templateId", template.getTemplateId());
            metadata.put("templateName", template.getName());
            metadata.put("documentName", doc.getName());
            metadata.put("templateVersion", version.getVersion());
            metadata.put("data", req.data());
            auditService.log(orgId, user, AuditEntityType.DOCUMENT, doc.getDocumentId(), AuditAction.DOCUMENT_GENERATED, metadata);
        }

        return pdf;
    }

    @PreAuthorize("hasPermission(#orgId, 'document:view')")
    public List<DocumentDTO> list(User user, Long orgId) {
        return documentRepository.findAllByOrg(orgId);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'document:generate')")
    public void finalise(User user, Long orgId, Long id) {
        Document doc = documentRepository
                .findByDocumentIdAndOrganisationOrganisationId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        if (doc.getStatus() == DocumentStatus.FINAL) return;

        DocumentStatus previousStatus = doc.getStatus();
        doc.setStatus(DocumentStatus.FINAL);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", doc.getDocumentId());
        metadata.put("documentName", doc.getName());
        metadata.put("previousStatus", previousStatus.name());
        metadata.put("newStatus", DocumentStatus.FINAL.name());
        auditService.log(orgId, user, AuditEntityType.DOCUMENT, doc.getDocumentId(), AuditAction.DOCUMENT_FINALISED, metadata);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'document:generate')")
    public void revert(User user, Long orgId, Long id) {
        Document doc = documentRepository
                .findByDocumentIdAndOrganisationOrganisationId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        if (doc.getStatus() == DocumentStatus.DRAFT) return;

        DocumentStatus previousStatus = doc.getStatus();
        doc.setStatus(DocumentStatus.DRAFT);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", doc.getDocumentId());
        metadata.put("documentName", doc.getName());
        metadata.put("previousStatus", previousStatus.name());
        metadata.put("newStatus", DocumentStatus.DRAFT.name());
        auditService.log(orgId, user, AuditEntityType.DOCUMENT, doc.getDocumentId(), AuditAction.DOCUMENT_REVERTED, metadata);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'document:view')")
    public byte[] view(User user, Long orgId, Long id) {
        Document doc = documentRepository
                .findByDocumentIdAndOrganisationOrganisationId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", doc.getDocumentId());
        metadata.put("documentName", doc.getName());
        metadata.put("documentStatus", doc.getStatus().name());
        auditService.log(orgId, user, AuditEntityType.DOCUMENT, doc.getDocumentId(), AuditAction.DOCUMENT_VIEWED, metadata);

        return doc.getFile();
    }

    private JsonNode parse(String manifest) {
        try {
            return objectMapper.readTree(manifest);
        } catch (Exception e) {
            throw new RuntimeException("Invalid manifest");
        }
    }

    private void validate(JsonNode manifest, Map<String, String> data) {
        Set<String> declaredFields = new java.util.HashSet<>();
        checkFields(manifest.get("fields"), data, declaredFields);
        checkFields(manifest.get("inline_fields"), data, declaredFields);

        for (String key : data.keySet()) {
            if (!declaredFields.contains(key)) {
                throw new IllegalArgumentException("Unrecognized field provided in document data: " + key);
            }
        }
    }

    private void checkFields(JsonNode fields, Map<String, String> data, Set<String> declaredFields) {
        if (fields == null) return;

        for (JsonNode f : fields) {
            String name = f.get("name").asString();
            declaredFields.add(name);
            boolean required = f.get("required").asBoolean(false);
            String val = data.get(name);

            if (val == null || val.isBlank()) {
                if (required) {
                    throw new IllegalArgumentException("Missing required field: " + name);
                }
                continue;
            }

            String type = f.has("type") ? f.get("type").asString().toLowerCase().trim() : "text";

            if ("text".equals(type)) {
                if (f.has("minLength") && !f.get("minLength").isNull()) {
                    int minLen = f.get("minLength").asInt();
                    if (val.length() < minLen) {
                        throw new IllegalArgumentException("Field '" + name + "' length is " + val.length() + " which violates minLength constraint of " + minLen);
                    }
                }
                if (f.has("maxLength") && !f.get("maxLength").isNull()) {
                    int maxLen = f.get("maxLength").asInt();
                    if (val.length() > maxLen) {
                        throw new IllegalArgumentException("Field '" + name + "' length is " + val.length() + " which violates maxLength constraint of " + maxLen);
                    }
                }
            } else if ("number".equals(type)) {
                double doubleVal;
                try {
                    doubleVal = Double.parseDouble(val.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Field '" + name + "' must be a valid number, but was: " + val);
                }

                if (f.has("minValue") && !f.get("minValue").isNull()) {
                    double min = f.get("minValue").asDouble();
                    if (doubleVal < min) {
                        throw new IllegalArgumentException("Field '" + name + "' value " + doubleVal + " violates minValue constraint of " + min);
                    }
                }
                if (f.has("maxValue") && !f.get("maxValue").isNull()) {
                    double max = f.get("maxValue").asDouble();
                    if (doubleVal > max) {
                        throw new IllegalArgumentException("Field '" + name + "' value " + doubleVal + " violates maxValue constraint of " + max);
                    }
                }
                if (f.has("decimalPlaces") && !f.get("decimalPlaces").isNull()) {
                    int maxDecimals = f.get("decimalPlaces").asInt();
                    String trimmed = val.trim();
                    int dotIdx = trimmed.indexOf('.');
                    int actualDecimals = 0;
                    if (dotIdx >= 0) {
                        String decimalsPart = trimmed.substring(dotIdx + 1).replaceAll("0+$", "");
                        actualDecimals = decimalsPart.length();
                    }
                    if (actualDecimals > maxDecimals) {
                        throw new IllegalArgumentException("Field '" + name + "' violates decimalPlaces constraint: maximum permitted is " + maxDecimals + ", but got " + actualDecimals);
                    }
                }
            }
        }
    }

    private String render(String html, Map<String, String> data) {
        String result = html;

        for (var entry : data.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace("{" + entry.getKey() + "}", value);
        }

        return LEFTOVER_PLACEHOLDER.matcher(result).replaceAll("");
    }

    private String resolveName(String name, String fallback) {
        return (name == null || name.isBlank()) ? fallback : name;
    }

    private String toJson(Map<String, String> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
}