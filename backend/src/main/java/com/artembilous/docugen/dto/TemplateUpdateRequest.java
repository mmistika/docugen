package com.artembilous.docugen.dto;

public record TemplateUpdateRequest(
        String name,
        String manifest,
        String content
) {}