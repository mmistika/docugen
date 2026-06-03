package com.artembilous.docugen.dto;

import com.artembilous.docugen.entity.AuditEntityType;

import com.fasterxml.jackson.annotation.JsonFormat;
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

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime timestamp
) {}