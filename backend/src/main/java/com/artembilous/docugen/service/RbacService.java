package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.PermissionDTO;
import com.artembilous.docugen.dto.RoleDTO;
import com.artembilous.docugen.dto.RoleUpdateRequest;
import com.artembilous.docugen.dto.UpdateMemberRolesRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.repository.PermissionRepository;
import com.artembilous.docugen.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final OrganisationRepository organisationRepository;
    private final MembershipRepository membershipRepository;
    private final AuditService auditService;

    @Transactional
    public Set<PermissionDTO> getPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionDTO(
                        p.getPermissionId(),
                        p.getName()
                )).collect(Collectors.toSet());
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public Set<RoleDTO> getRoles(User user, Long orgId) {
        return roleRepository.findByOrganisationOrganisationId(orgId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toSet());
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    @CacheEvict(value = "user_permissions", allEntries = true)
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

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orgId", orgId);
        metadata.put("roleName", req.name());
        metadata.put("permissions", req.permissions().stream().toList());
        auditService.log(orgId, user, AuditEntityType.ROLE, role.getRoleId(), AuditAction.ROLE_CREATED, metadata);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    @CacheEvict(value = "user_permissions", allEntries = true)
    public void updateRole(User user, Long orgId, RoleUpdateRequest req) {
        if ("ADMIN".equals(req.name())) {
            throw new IllegalStateException("Cannot modify ADMIN role");
        }

        Role role = roleRepository
                .findByNameAndOrganisationOrganisationId(req.name(), orgId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        role.setName(req.name());
        role.setPermissions(resolvePermissions(req.permissions()));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orgId", orgId);
        metadata.put("roleName", req.name());
        metadata.put("permissions", req.permissions().stream().toList());
        auditService.log(orgId, user, AuditEntityType.ROLE, role.getRoleId(), AuditAction.ROLE_UPDATED, metadata);
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    @CacheEvict(value = "user_permissions", allEntries = true)
    public void deleteRole(User user, Long orgId, String roleName) {
        if ("ADMIN".equals(roleName)) {
            throw new IllegalStateException("Cannot delete ADMIN role");
        }

        Role role = roleRepository
                .findByNameAndOrganisationOrganisationId(roleName, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        roleRepository.delete(role);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orgId", orgId);
        metadata.put("roleName", roleName);
        auditService.log(orgId, user, AuditEntityType.ROLE, role.getRoleId(), AuditAction.ROLE_DELETED, metadata);
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

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'members:manage')")
    @CacheEvict(value = "user_permissions", allEntries = true)
    public void updateMemberRoles(User user, Long orgId, Long memberId, UpdateMemberRolesRequest req) {
        Membership membership = membershipRepository
                .findByMembershipIdAndOrganisationOrganisationId(memberId, orgId)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found"));

        Set<Role> roles = roleRepository
                .findByOrganisationOrganisationIdAndNameIn(orgId, req.roles());
        if (roles.size() != req.roles().size()) {
            throw new IllegalArgumentException("Invalid role provided");
        }

        boolean newAdminPresent = roles.stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));

        boolean currentlyAdmin = membership.getRoles()
                .stream()
                .anyMatch(r -> r.getName().equals("ADMIN"));

        if (currentlyAdmin && !newAdminPresent) {
            boolean anotherAdminExists =
                    membershipRepository.existsAnotherAdmin(orgId, memberId);

            if (!anotherAdminExists) {
                throw new IllegalStateException(
                        "Organisation must have at least one ADMIN"
                );
            }
        }

        List<String> newRoles = roles.stream().map(Role::getName).toList();

        membership.setRoles(roles);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orgId", orgId);
        metadata.put("memberId", memberId);
        metadata.put("memberEmail", membership.getUser().getEmail());
        metadata.put("newRoles", newRoles);
        auditService.log(orgId, user, AuditEntityType.MEMBERSHIP, memberId, AuditAction.MEMBER_ROLES_UPDATED, metadata);
    }
}