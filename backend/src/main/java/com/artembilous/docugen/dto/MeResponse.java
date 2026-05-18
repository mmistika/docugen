package com.artembilous.docugen.dto;

public record MeResponse (
    Long userId,
    String email,
    String name,
    String surname,
    boolean registered,
    String image
) {}