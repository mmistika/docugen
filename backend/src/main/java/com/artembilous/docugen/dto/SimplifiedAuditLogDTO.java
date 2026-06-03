package com.artembilous.docugen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record SimplifiedAuditLogDTO(
        Long id,
        String action,
        String userName,
        String userEmail,
        Boolean isToken,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime timestamp
) {
}
