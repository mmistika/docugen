package com.artembilous.docugen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganisationRequest (
    @NotBlank(message = "Organisation name is required")
    @Size(max = 255, message = "Organisation name must be at most 255 characters")
    String name
) {}