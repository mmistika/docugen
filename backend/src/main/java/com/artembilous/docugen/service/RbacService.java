package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.PermissionDTO;
import com.artembilous.docugen.dto.RoleDTO;
import com.artembilous.docugen.dto.RoleUpdateRequest;
import com.artembilous.docugen.entity.Organisation;
import com.artembilous.docugen.entity.Permission;
import com.artembilous.docugen.entity.Role;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.repository.PermissionRepository;
import com.artembilous.docugen.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final OrganisationRepository organisationRepository;

    public Set<PermissionDTO> getPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionDTO(
                        p.getPermissionId(),
                        p.getName()
                )).collect(Collectors.toSet());
    }

    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public Set<RoleDTO> getRoles(User user, Long orgId) {
        return roleRepository.findByOrganisationOrganisationId(orgId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public void createRole(User user, Long orgId, RoleUpdateRequest req) {
        if ("ADMIN".equals(req.name())) {
            throw new IllegalArgumentException("Cannot create ADMIN role");
        }

        Organisation org = organisationRepository.getReferenceById(orgId);
        roleRepository.findByNameAndOrganisation(req.name(), org)
                .ifPresent(_ -> {
                    throw new IllegalStateException("Role already exists");
                });

        Role role = new Role();
        role.setName(req.name());
        role.setOrganisation(org);

        Set<Permission> permissions = resolvePermissions(req.permissions());
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public void updateRole(User user, Long orgId, RoleUpdateRequest req) {
        if ("ADMIN".equals(req.name())) {
            throw new IllegalStateException("Cannot modify ADMIN role");
        }

        Role role = roleRepository
                .findByNameAndOrganisationOrganisationId(req.name(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        role.setName(req.name());
        role.setPermissions(resolvePermissions(req.permissions()));
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public void deleteRole(User user, Long orgId, String roleName) {
        if ("ADMIN".equals(roleName)) {
            throw new IllegalStateException("Cannot delete ADMIN role");
        }

        Role role = roleRepository
                .findByNameAndOrganisationOrganisationId(roleName, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        roleRepository.delete(role);
    }

    private RoleDTO toDTO(Role role) {
        return new RoleDTO(
                role.getName(),
                role.getPermissions()
                        .stream()
                        .map(Permission::getName)
                        .collect(Collectors.toSet())
        );
    }

    private Set<Permission> resolvePermissions(Set<String> names) {
        Set<Permission> permissions = permissionRepository.findByNameIn(names);
        if (permissions.size() != names.size()) {
            throw new IllegalArgumentException("Invalid permission provided");
        }
        return permissions;
    }
}