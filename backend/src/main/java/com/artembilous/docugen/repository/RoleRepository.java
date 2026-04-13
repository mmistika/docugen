package com.artembilous.docugen.repository;

import com.artembilous.docugen.entity.Organisation;
import com.artembilous.docugen.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNameAndOrganisation(String name, Organisation orgId);

    Optional<Role> findByNameAndOrganisationOrganisationId(String name, Long orgId);

    Set<Role> findByOrganisationOrganisationId(Long orgId);
}