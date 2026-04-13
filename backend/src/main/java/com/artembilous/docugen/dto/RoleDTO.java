package com.artembilous.docugen.dto;

import java.util.Set;

public record RoleDTO(
        String name,
        Set<String> permissions
) {}