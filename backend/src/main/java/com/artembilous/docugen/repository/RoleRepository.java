package com.artembilous.docugen.repository;

import com.artembilous.docugen.entity.Organisation;
import com.artembilous.docugen.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNameAndOrganisation(String name, Organisation orgId);
}