package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.CreateOrganisationRequest;
import com.artembilous.docugen.dto.MemberDTO;
import com.artembilous.docugen.dto.OrganisationDTO;
import com.artembilous.docugen.dto.RenameOrganisationRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.exception.RegistrationIncompleteException;
import com.artembilous.docugen.service.OrganisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService orgService;

    @GetMapping("/my")
    public List<OrganisationDTO> my(@AuthenticationPrincipal User user) {
        return orgService.getUserOrganisations(user);
    }

    @PostMapping
    public void create(@AuthenticationPrincipal User user, @Valid @RequestBody CreateOrganisationRequest req) {
        if (user.getName() == null || user.getSurname() == null) {
            throw new RegistrationIncompleteException("Complete name/surname registration step first");
        }

        orgService.createOrganisation(user, req.name());
    }

    @GetMapping("/{orgId}/members")
    @PreAuthorize("hasPermission(#orgId, 'members:manage')")
    public List<MemberDTO> getMembers(@AuthenticationPrincipal User user, @PathVariable Long orgId) {
        return orgService.getOrganisationMembers(orgId);
    }

    @PatchMapping("/{orgId}")
    public void rename(@AuthenticationPrincipal User user, @PathVariable Long orgId, @Valid @RequestBody RenameOrganisationRequest req) {
        orgService.rename(user, orgId, req);
    }
}