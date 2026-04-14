package com.artembilous.docugen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateMemberRolesRequest(
        @NotEmpty(message = "The role set is required")
        Set<@NotBlank(message = "Valid roles are required")String> roles
) {}