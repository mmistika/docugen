package com.artembilous.docugen.dto;

import java.util.Map;

public record GenerateDocumentRequest(
        Long templateId,
        Map<String, String> data,
        String name
) {}