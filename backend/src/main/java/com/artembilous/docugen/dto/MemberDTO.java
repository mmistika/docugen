package com.artembilous.docugen.dto;

import java.util.List;

public record MemberDTO(
        Long id,
        String name,
        String surname,
        String email,
        List<String> roles
) {}
