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

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final PdfService pdfService;

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
        doc.setStatus(DocumentStatus.FINAL);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'document:generate')")
    public void revert(User user, Long orgId, Long id) {
        Document doc = documentRepository
                .findByDocumentIdAndOrganisationOrganisationId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        if (doc.getStatus() == DocumentStatus.DRAFT) return;
        doc.setStatus(DocumentStatus.DRAFT);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'document:view')")
    public byte[] view(User user, Long orgId, Long id) {
        Document doc = documentRepository
                .findByDocumentIdAndOrganisationOrganisationId(id, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

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
        check(manifest.get("fields"), data);
        check(manifest.get("inline_fields"), data);
    }

    private void check(JsonNode fields, Map<String, String> data) {
        if (fields == null) return;

        for (JsonNode f : fields) {
            String name = f.get("name").asString();
            boolean required = f.get("required").asBoolean(false);

            if (required && (data.get(name) == null || data.get(name).isBlank())) {
                throw new IllegalArgumentException("Missing required field: " + name);
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