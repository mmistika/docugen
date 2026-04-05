package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.CreateOrganisationRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.exception.RegistrationIncompleteException;
import com.artembilous.docugen.service.OrganisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService orgService;

    @PostMapping
    public void create(@AuthenticationPrincipal User user, @Valid @RequestBody CreateOrganisationRequest req) {
        if (user.getName() == null || user.getSurname() == null) {
            throw new RegistrationIncompleteException("Complete name/surname registration step first");
        }

        orgService.createOrganisation(user, req.name());
    }

    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    @GetMapping("/{orgId}")
    public String test(@PathVariable Long orgId) {
        return "OK";
    }
}