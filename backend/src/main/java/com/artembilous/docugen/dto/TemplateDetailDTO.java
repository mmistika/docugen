package com.artembilous.docugen.dto;

import com.artembilous.docugen.entity.TemplateVersionStatus;

public record TemplateDetailDTO(
        Long id,
        String name,
        Integer version,
        String manifest,
        String content,
        TemplateVersionStatus status
) {}