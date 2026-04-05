package com.artembilous.docugen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompleteRegistrationRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 25, message = "Name must be at most 25 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(max = 25, message = "Surname must be at most 25 characters")
        String surname
) {}