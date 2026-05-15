package com.artembilous.docugen.dto;

import com.artembilous.docugen.entity.AuditEntityType;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        String action,
        AuditEntityType entityType,
        Long entityId,

        Long userId,
        String userEmail,
        String userName,
        Boolean isToken,

        String metadata,

        LocalDateTime timestamp
) {}