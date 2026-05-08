package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.MemberDTO;
import com.artembilous.docugen.dto.OrganisationDTO;
import com.artembilous.docugen.dto.RenameOrganisationRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository orgRepo;
    private final MembershipRepository membershipRepo;
    private final RoleRepository roleRepo;
    private final AuditService auditService;

    public List<OrganisationDTO> getUserOrganisations(User user) {
        return membershipRepo.findUserOrganisations(user);
    }

    @Transactional
    public Organisation createOrganisation(User user, String name) {
        Organisation org = new Organisation();
        org.setName(name);
        org = orgRepo.save(org);

        Role adminRole = roleRepo.findByNameAndOrganisation("ADMIN", org)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve ADMIN role"));

        Membership m = new Membership();
        m.setUser(user);
        m.setOrganisation(org);
        m.setRoles(Set.of(adminRole));
        membershipRepo.save(m);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orgId", org.getOrganisationId());
        metadata.put("orgName", org.getName());
        auditService.log(org.getOrganisationId(), user, AuditEntityType.ORGANISATION, org.getOrganisationId(), AuditAction.ORGANISATION_CREATED, metadata);

        return org;
    }

    @Transactional
    public List<MemberDTO> getOrganisationMembers(Long orgId) {
        List<Membership> memberships = membershipRepo.findByOrganisationOrganisationId(orgId);
        return memberships.stream()
                .map(m -> new MemberDTO(
                        m.getMembershipId(),
                        m.getUser().getName(),
                        m.getUser().getSurname(),
                        m.getUser().getEmail(),
                        m.getRoles().stream().map(Role::getName).toList()
                ))
                .toList();
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public void rename(User user, Long orgId, RenameOrganisationRequest req) {
        Organisation org = orgRepo
                .findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        if (req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        String oldName = org.getName();
        org.setName(req.name());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orgId", org.getOrganisationId());
        metadata.put("oldName", oldName);
        metadata.put("newName", req.name());
        auditService.log(orgId, user, AuditEntityType.ORGANISATION, org.getOrganisationId(), AuditAction.ORGANISATION_RENAMED, metadata);
    }
}