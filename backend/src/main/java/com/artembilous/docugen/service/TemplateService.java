package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.TemplateDTO;
import com.artembilous.docugen.dto.TemplateDetailDTO;
import com.artembilous.docugen.dto.TemplateUpdateRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.TemplateRepository;
import com.artembilous.docugen.repository.TemplateVersionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;
    private final AuditService auditService;

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
}