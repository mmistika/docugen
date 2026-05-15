package com.artembilous.docugen.dto;

import java.time.LocalDateTime;

public record SimplifiedAuditLogDTO(
        Long id,
        String action,
        String userName,
        String userEmail,
        Boolean isToken,
        LocalDateTime timestamp
) {
}
