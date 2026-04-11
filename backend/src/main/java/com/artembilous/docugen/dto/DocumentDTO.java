package com.artembilous.docugen.dto;

import com.artembilous.docugen.entity.DocumentStatus;

import java.time.LocalDateTime;

public record DocumentDTO(
        Long id,
        String name,
        DocumentStatus status,
        String templateName,
        LocalDateTime createdAt
) {}