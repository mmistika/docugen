package com.artembilous.docugen.dto;

import com.artembilous.docugen.entity.DocumentStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record DocumentDTO(
        Long id,
        String name,
        DocumentStatus status,
        String templateName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime createdAt
) {}