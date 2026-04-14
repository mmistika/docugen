package com.artembilous.docugen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteUserRequest(

        @NotNull(message = "Organisation ID is required")
        Long orgId,

        @Email(message = "Valid email is required")
        String email,

        @NotBlank(message = "Role name is required")
        String role
) {}