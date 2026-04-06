package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.OrganisationDTO;
import com.artembilous.docugen.entity.Membership;
import com.artembilous.docugen.entity.Organisation;
import com.artembilous.docugen.entity.Role;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository orgRepo;
    private final MembershipRepository membershipRepo;
    private final RoleRepository roleRepo;

    public List<OrganisationDTO> getUserOrganisations(User user) {
        return membershipRepo.findUserOrganisations(user);
    }

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

        return org;
    }
}