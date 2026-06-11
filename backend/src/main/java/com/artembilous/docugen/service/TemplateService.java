/*
 * Docugen — Document Generation & Management Platform
 * Copyright (C) 2026 Artem Bilous
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.TemplateDTO;
import com.artembilous.docugen.dto.TemplateDetailDTO;
import com.artembilous.docugen.dto.TemplateUpdateRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.TemplateRepository;
import com.artembilous.docugen.repository.TemplateVersionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasPermission(#orgId, 'template:view')")
    public List<TemplateDTO> getAll(User user, Long orgId) {
        return templateRepository.findAllByOrg(orgId);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'template:update')")
    public TemplateDetailDTO getForUpdate(User user, Long orgId, Long templateId) {

        Template template = templateRepository
                .findByTemplateIdAndOrganisation_OrganisationId(templateId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("No template found"));

        TemplateVersion version = versionRepository
                .findLatest(templateId)
                .orElseThrow(() -> new EntityNotFoundException("No usable version found"));

        return new TemplateDetailDTO(
                template.getTemplateId(),
                template.getName(),
                version.getVersion(),
                version.getManifest(),
                version.getContent(),
                version.getStatus()
        );
    }

    @PreAuthorize("hasPermission(#orgId, 'document:generate')")
    public TemplateDetailDTO getActive(User user, Long orgId, Long templateId) {
        Template template = templateRepository
                .findByTemplateIdAndOrganisation_OrganisationId(templateId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("No template found"));

        TemplateVersion active = versionRepository
                .findByTemplateTemplateIdAndStatus(templateId, TemplateVersionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active version available"));

        return new TemplateDetailDTO(
                template.getTemplateId(),
                template.getName(),
                active.getVersion(),
                active.getManifest(),
                active.getContent(),
                active.getStatus()
        );
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'template:update')")
    public Long update(User user, Long orgId, Long templateId, TemplateUpdateRequest req) {
        validateTemplate(req.manifest(), req.content());

        Template template;
        boolean isNewTemplate = (templateId == null);

        if (isNewTemplate) {
            template = new Template();
            template.setName(req.name());

            Organisation org = new Organisation();
            org.setOrganisationId(orgId);
            template.setOrganisation(org);

            template = templateRepository.save(template);

        } else {
            template = templateRepository
                    .findByTemplateIdAndOrganisation_OrganisationId(templateId, orgId)
                    .orElseThrow(() -> new EntityNotFoundException("No template found on update"));
        }

        Optional<TemplateVersion> existingDraft =
                versionRepository.findByTemplateTemplateIdAndStatus(
                        template.getTemplateId(),
                        TemplateVersionStatus.DRAFT
                );

        int versionNumber;
        if (existingDraft.isPresent()) {
            TemplateVersion draft = existingDraft.get();
            draft.setManifest(req.manifest());
            draft.setContent(req.content());
            versionNumber = draft.getVersion();
        } else {
            versionNumber = versionRepository.findLatest(template.getTemplateId())
                    .stream()
                    .findFirst()
                    .map(v -> v.getVersion() + 1)
                    .orElse(1);

            TemplateVersion version = new TemplateVersion();
            version.setTemplate(template);
            version.setVersion(versionNumber);
            version.setManifest(req.manifest());
            version.setContent(req.content());
            version.setStatus(TemplateVersionStatus.DRAFT);

            versionRepository.save(version);
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("templateId", template.getTemplateId());
        metadata.put("templateName", template.getName());
        metadata.put("version", versionNumber);

        auditService.log(
                orgId, user, AuditEntityType.TEMPLATE, template.getTemplateId(),
                isNewTemplate? AuditAction.TEMPLATE_CREATED : AuditAction.TEMPLATE_UPDATED,
                metadata
        );

        return template.getTemplateId();
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'template:update')")
    public void publish(User user, Long orgId, Long templateId) {

        Template template = templateRepository
                .findByTemplateIdAndOrganisation_OrganisationId(templateId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("No template found to publish"));

        TemplateVersion draft = versionRepository
                .findByTemplateTemplateIdAndStatus(templateId, TemplateVersionStatus.DRAFT)
                .orElseThrow(() -> new IllegalStateException("No draft version to publish"));

        Optional<TemplateVersion> activeOpt =
                versionRepository.findByTemplateTemplateIdAndStatus(templateId, TemplateVersionStatus.ACTIVE);

        activeOpt.ifPresent(active -> {
            active.setStatus(TemplateVersionStatus.RETIRED);
        });

        draft.setStatus(TemplateVersionStatus.ACTIVE);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("templateId", template.getTemplateId());
        metadata.put("templateName", template.getName());
        metadata.put("version", draft.getVersion());
        auditService.log(orgId, user, AuditEntityType.TEMPLATE, template.getTemplateId(), AuditAction.TEMPLATE_PUBLISHED, metadata);
    }

    private void validateTemplate(String manifestStr, String content) {
        if (manifestStr == null || manifestStr.isBlank()) {
            throw new IllegalArgumentException("Template manifest cannot be empty");
        }

        JsonNode manifest;
        try {
            manifest = objectMapper.readTree(manifestStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid manifest JSON format: " + e.getMessage(), e);
        }

        Set<String> declaredFields = new HashSet<>();
        validateFieldsList(manifest.get("fields"), declaredFields);
        validateFieldsList(manifest.get("inline_fields"), declaredFields);

        // Validate placeholders in content HTML
        if (content != null) {
            Matcher matcher = Pattern.compile("\\{([a-zA-Z0-9_.-]+)}").matcher(content);
            while (matcher.find()) {
                String placeholder = matcher.group(1);
                if (!declaredFields.contains(placeholder)) {
                    throw new IllegalArgumentException("Placeholder '" + placeholder + "' found in template content is not declared in the manifest");
                }
            }
        }
    }

    private void validateFieldsList(JsonNode fieldsNode, Set<String> declaredFields) {
        if (fieldsNode == null) return;
        if (!fieldsNode.isArray()) {
            throw new IllegalArgumentException("Fields metadata must be a JSON array");
        }

        for (JsonNode field : fieldsNode) {
            JsonNode nameNode = field.get("name");
            if (nameNode == null || nameNode.asString().isBlank()) {
                throw new IllegalArgumentException("Field name cannot be empty");
            }
            String fieldName = nameNode.asString().trim();
            if (declaredFields.contains(fieldName)) {
                throw new IllegalArgumentException("Duplicate field name declared in manifest: " + fieldName);
            }
            declaredFields.add(fieldName);

            JsonNode typeNode = field.get("type");
            if (typeNode == null || typeNode.asString().isBlank()) {
                throw new IllegalArgumentException("Field '" + fieldName + "' must have a valid type");
            }
            String type = typeNode.asString().toLowerCase().trim();
            if (!Set.of("text", "number").contains(type)) {
                throw new IllegalArgumentException("Field '" + fieldName + "' has invalid type: " + type);
            }

            // Validate constraints types if specified
            if ("text".equals(type)) {
                if (field.has("minLength") && !field.get("minLength").isNull() && !field.get("minLength").isNumber()) {
                    throw new IllegalArgumentException("Field '" + fieldName + "' minLength must be a valid number");
                }
                if (field.has("maxLength") && !field.get("maxLength").isNull() && !field.get("maxLength").isNumber()) {
                    throw new IllegalArgumentException("Field '" + fieldName + "' maxLength must be a valid number");
                }
            } else if ("number".equals(type)) {
                if (field.has("minValue") && !field.get("minValue").isNull() && !field.get("minValue").isNumber()) {
                    throw new IllegalArgumentException("Field '" + fieldName + "' minValue must be a valid number");
                }
                if (field.has("maxValue") && !field.get("maxValue").isNull() && !field.get("maxValue").isNumber()) {
                    throw new IllegalArgumentException("Field '" + fieldName + "' maxValue must be a valid number");
                }
                if (field.has("decimalPlaces") && !field.get("decimalPlaces").isNull() && !field.get("decimalPlaces").isNumber()) {
                    throw new IllegalArgumentException("Field '" + fieldName + "' decimalPlaces must be a valid number");
                }
            }
        }
    }
}