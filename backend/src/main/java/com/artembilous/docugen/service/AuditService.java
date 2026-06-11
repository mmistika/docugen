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

import com.artembilous.docugen.dto.AuditLogDTO;
import com.artembilous.docugen.entity.AuditAction;
import com.artembilous.docugen.entity.AuditEntityType;
import com.artembilous.docugen.entity.AuditLog;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.ApiTokenRepository;
import com.artembilous.docugen.repository.AuditLogRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.security.ApiTokenAuthenticationToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditRepository;
    private final OrganisationRepository organisationRepository;
    private final ApiTokenRepository apiTokenRepository;

    private final ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private AuditService self;

    public void log(
            Long orgId,
            User actor,
            AuditEntityType entityType,
            Long entityId,
            AuditAction action,
            Object metadata
    ) {
        Long tokenId = null;
        String tokenName = null;
        if (actor == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof ApiTokenAuthenticationToken apiTokenAuth) {
                tokenId = apiTokenAuth.getTokenId();
                tokenName = apiTokenAuth.getTokenName();
            }
        }
        self.logAsync(orgId, actor, tokenId, tokenName, entityType, entityId, action, metadata);
    }

    @Async
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void logAsync(
            Long orgId,
            User actor,
            Long tokenId,
            String tokenName,
            AuditEntityType entityType,
            Long entityId,
            AuditAction action,
            Object metadata
    ) {

        AuditLog log = new AuditLog();

        log.setOrganisation(
                organisationRepository.getReferenceById(orgId)
        );

        if (actor != null) {
            log.setUser(actor);
        } else if (tokenId != null) {
            log.setApiToken(apiTokenRepository.getReferenceById(tokenId));
            log.setTokenName(tokenName);
        }

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
                .map(log -> {
                    boolean isToken = log.getTokenName() != null;
                    String userName = log.getUser() != null
                            ? log.getUser().getName() + " " + log.getUser().getSurname()
                            : (isToken ? log.getTokenName() : "System");
                    String userEmail = log.getUser() != null ? log.getUser().getEmail() : null;

                    return new AuditLogDTO(
                            log.getLogId(),
                            log.getAction().name(),
                            log.getEntityType(),
                            log.getEntityId(),

                            log.getUser() != null ? log.getUser().getUserId() : null,
                            userEmail,
                            userName,
                            isToken,

                            log.getMetadata(),
                            log.getTimestamp()
                    );
                });
    }
}