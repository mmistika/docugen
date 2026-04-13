package com.artembilous.docugen.repository;

import com.artembilous.docugen.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Set<Permission> findByNameIn(Set<String> names);
}