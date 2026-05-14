package com.artembilous.docugen.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record CreateApiTokenResponse(
        Long id,
        String name,
        String rawToken,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Set<String> permissions
) {
}
