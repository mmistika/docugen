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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RbacServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RbacService rbacService;

    private User testUser;
    private Organisation testOrg;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        testOrg = new Organisation();
        testOrg.setOrganisationId(10L);

        testRole = new Role();
        testRole.setRoleId(100L);
        testRole.setName("DEVELOPER");
        testRole.setOrganisation(testOrg);
        testRole.setPermissions(new java.util.HashSet<>());
    }

    @Test
    void getPermissions_ShouldReturnAll() {
        Permission p = new Permission();
        p.setPermissionId(1L);
        p.setName("template:view");

        when(permissionRepository.findAll()).thenReturn(List.of(p));

        Set<PermissionDTO> result = rbacService.getPermissions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("template:view", result.iterator().next().name());
    }

    @Test
    void getRoles_ShouldReturnRolesForOrg() {
        when(roleRepository.findByOrganisationOrganisationId(10L)).thenReturn(Set.of(testRole));

        Set<RoleDTO> result = rbacService.getRoles(testUser, 10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DEVELOPER", result.iterator().next().name());
    }

    @Test
    void createRole_WithAdminName_ShouldThrowIllegalArgumentException() {
        RoleUpdateRequest req = new RoleUpdateRequest("ADMIN", Set.of());
        assertThrows(IllegalArgumentException.class, () -> rbacService.createRole(testUser, 10L, req));
    }

    @Test
    void createRole_WhenAlreadyExists_ShouldThrowIllegalStateException() {
        RoleUpdateRequest req = new RoleUpdateRequest("DEVELOPER", Set.of());
        when(organisationRepository.getReferenceById(10L)).thenReturn(testOrg);
        when(roleRepository.findByNameAndOrganisation("DEVELOPER", testOrg)).thenReturn(Optional.of(testRole));

        assertThrows(IllegalStateException.class, () -> rbacService.createRole(testUser, 10L, req));
    }

    @Test
    void createRole_WithValidInput_ShouldSaveAndLog() {
        RoleUpdateRequest req = new RoleUpdateRequest("NEW_ROLE", Set.of("template:view"));
        Permission p = new Permission();
        p.setName("template:view");

        when(organisationRepository.getReferenceById(10L)).thenReturn(testOrg);
        when(roleRepository.findByNameAndOrganisation("NEW_ROLE", testOrg)).thenReturn(Optional.empty());
        when(permissionRepository.findByNameIn(req.permissions())).thenReturn(Set.of(p));

        rbacService.createRole(testUser, 10L, req);

        verify(roleRepository, times(1)).save(any(Role.class));
        verify(auditService, times(1)).log(eq(10L), eq(testUser), eq(AuditEntityType.ROLE), any(), eq(AuditAction.ROLE_CREATED), any());
    }

    @Test
    void updateRole_WithAdminName_ShouldThrowIllegalStateException() {
        RoleUpdateRequest req = new RoleUpdateRequest("ADMIN", Set.of());
        assertThrows(IllegalStateException.class, () -> rbacService.updateRole(testUser, 10L, req));
    }

    @Test
    void deleteRole_WithAdminName_ShouldThrowIllegalStateException() {
        assertThrows(IllegalStateException.class, () -> rbacService.deleteRole(testUser, 10L, "ADMIN"));
    }

    @Test
    void updateMemberRoles_WhenChangingLastAdminRole_ShouldThrowIllegalStateException() {
        User memberUser = new User();
        memberUser.setEmail("member@example.com");

        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        Membership membership = new Membership();
        membership.setUser(memberUser);
        membership.setRoles(Set.of(adminRole));

        UpdateMemberRolesRequest req = new UpdateMemberRolesRequest(Set.of("DEVELOPER"));

        when(membershipRepository.findByMembershipIdAndOrganisationOrganisationId(200L, 10L))
                .thenReturn(Optional.of(membership));
        when(roleRepository.findByOrganisationOrganisationIdAndNameIn(10L, req.roles()))
                .thenReturn(Set.of(testRole));
        when(membershipRepository.existsAnotherAdmin(10L, 200L)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> rbacService.updateMemberRoles(testUser, 10L, 200L, req));
    }

    @Test
    void updateMemberRoles_WithValidTransition_ShouldSaveAndLog() {
        User memberUser = new User();
        memberUser.setEmail("member@example.com");

        Membership membership = new Membership();
        membership.setUser(memberUser);
        membership.setRoles(Set.of(testRole));

        UpdateMemberRolesRequest req = new UpdateMemberRolesRequest(Set.of("DEVELOPER"));

        when(membershipRepository.findByMembershipIdAndOrganisationOrganisationId(200L, 10L))
                .thenReturn(Optional.of(membership));
        when(roleRepository.findByOrganisationOrganisationIdAndNameIn(10L, req.roles()))
                .thenReturn(Set.of(testRole));

        rbacService.updateMemberRoles(testUser, 10L, 200L, req);

        verify(auditService, times(1)).log(eq(10L), eq(testUser), eq(AuditEntityType.MEMBERSHIP), eq(200L), eq(AuditAction.MEMBER_ROLES_UPDATED), any());
    }
}
