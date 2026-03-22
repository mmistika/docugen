package com.artembilous.docugen.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user")
    public String me(@AuthenticationPrincipal Jwt jwt) {
        return jwt.getClaim("docugen-api/email");
    }
}
