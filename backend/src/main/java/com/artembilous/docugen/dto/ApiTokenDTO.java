package com.artembilous.docugen.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record ApiTokenDTO(
        Long id,
        String name,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Set<String> permissions
) {
}
