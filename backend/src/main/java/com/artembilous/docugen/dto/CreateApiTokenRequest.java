package com.artembilous.docugen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.Set;

public record CreateApiTokenRequest(
        @NotBlank(message = "API token name is required")
        String name,

        @NotEmpty(message = "The permission set is required")
        Set<@NotBlank(message = "Valid permissions are required") String> permissions,

        LocalDateTime expiresAt
) {
}
