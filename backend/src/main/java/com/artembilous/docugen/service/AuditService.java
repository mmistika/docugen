package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.AuditLogDTO;
import com.artembilous.docugen.entity.AuditAction;
import com.artembilous.docugen.entity.AuditEntityType;
import com.artembilous.docugen.entity.AuditLog;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.AuditLogRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepository;
    private final OrganisationRepository organisationRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public void log(
            Long orgId,
            User actor,
            AuditEntityType entityType,
            Long entityId,
            AuditAction action,
            Object metadata
    ) {

        AuditLog log = new AuditLog();

        log.setOrganisation(
                organisationRepository.getReferenceById(orgId)
        );

        log.setUser(actor);

        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);

        try {
            log.setMetadata(
                    metadata == null
                            ? ""
                            : objectMapper.writeValueAsString(metadata)
            );
        } catch (JacksonException e) {
            //throw new RuntimeException(e);
        }

        auditRepository.save(log);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public Page<AuditLogDTO> getLogs(
            User user,
            Long orgId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {

        return auditRepository.findAll(orgId, from, to, pageable)
                .map(log -> new AuditLogDTO(
                        log.getLogId(),
                        log.getAction().name(),
                        log.getEntityType(),
                        log.getEntityId(),

                        log.getUser().getUserId(),
                        log.getUser().getEmail(),
                        log.getUser().getName() + " " + log.getUser().getSurname(),

                        log.getMetadata(),
                        log.getTimestamp()
                ));
    }
}