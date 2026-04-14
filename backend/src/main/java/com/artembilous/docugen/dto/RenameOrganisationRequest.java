package com.artembilous.docugen.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameOrganisationRequest(
        @NotBlank(message = "Organisation name is required")
        String name
) {}