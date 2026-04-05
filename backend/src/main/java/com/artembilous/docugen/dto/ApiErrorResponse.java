package com.artembilous.docugen.dto;

public record ApiErrorResponse(
    int status,
    String errorCode,
    String message,
    long timestamp
) {}
