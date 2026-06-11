/*
 * Docugen — Document Generation & Management Platform
 * Copyright (C) 2026 Artem Bilous
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.MemberDTO;
import com.artembilous.docugen.dto.OrganisationDTO;
import com.artembilous.docugen.dto.RenameOrganisationRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
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
class OrganisationServiceTest {

    @Mock
    private OrganisationRepository orgRepo;

    @Mock
    private MembershipRepository membershipRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private OrganisationService organisationService;

    private User testUser;
    private Organisation testOrg;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setEmail("john@example.com");

        testOrg = new Organisation();
        testOrg.setOrganisationId(10L);
        testOrg.setName("Test Org");
    }

    @Test
    void getUserOrganisations_ShouldReturnList() {
        OrganisationDTO dto = new OrganisationDTO(10L, "Test Org", 1L);
        when(membershipRepo.findUserOrganisations(testUser)).thenReturn(List.of(dto));

        List<OrganisationDTO> result = organisationService.getUserOrganisations(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Org", result.getFirst().name());
    }

    @Test
    void createOrganisation_ShouldSaveOrgAndMembership() {
        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        when(orgRepo.save(any(Organisation.class))).thenAnswer(invocation -> {
            Organisation org = invocation.getArgument(0);
            org.setOrganisationId(10L);
            return org;
        });
        when(roleRepo.findByNameAndOrganisation(eq("ADMIN"), any(Organisation.class)))
                .thenReturn(Optional.of(adminRole));

        organisationService.createOrganisation(testUser, "New Org");

        verify(orgRepo, times(1)).save(any(Organisation.class));
        verify(membershipRepo, times(1)).save(argThat(m -> {
            assertEquals(testUser, m.getUser());
            assertEquals(10L, m.getOrganisation().getOrganisationId());
            assertTrue(m.getRoles().contains(adminRole));
            return true;
        }));
        verify(auditService, times(1)).log(eq(10L), eq(testUser), eq(AuditEntityType.ORGANISATION), eq(10L), eq(AuditAction.ORGANISATION_CREATED), any());
    }

    @Test
    void getOrganisationMembers_ShouldReturnMappedMemberDtos() {
        byte[] imageBytes = new byte[]{1, 2, 3};
        User memberUser = new User();
        memberUser.setName("Jane");
        memberUser.setSurname("Smith");
        memberUser.setEmail("jane@example.com");
        memberUser.setImage(imageBytes);

        Role role = new Role();
        role.setName("DEVELOPER");

        Membership membership = new Membership();
        membership.setMembershipId(200L);
        membership.setUser(memberUser);
        membership.setRoles(Set.of(role));

        when(membershipRepo.findByOrganisationOrganisationId(10L)).thenReturn(List.of(membership));

        List<MemberDTO> result = organisationService.getOrganisationMembers(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        MemberDTO dto = result.getFirst();
        assertEquals(200L, dto.id());
        assertEquals("Jane", dto.name());
        assertEquals("Smith", dto.surname());
        assertEquals("jane@example.com", dto.email());
        assertTrue(dto.roles().contains("DEVELOPER"));
        assertTrue(dto.image().startsWith("data:image/png;base64,"));
    }

    @Test
    void rename_WithValidName_ShouldRenameAndLog() {
        when(orgRepo.findById(10L)).thenReturn(Optional.of(testOrg));

        organisationService.rename(testUser, 10L, new RenameOrganisationRequest("Renamed Org"));

        assertEquals("Renamed Org", testOrg.getName());
        verify(auditService, times(1)).log(eq(10L), eq(testUser), eq(AuditEntityType.ORGANISATION), eq(10L), eq(AuditAction.ORGANISATION_RENAMED), any());
    }

    @Test
    void rename_WhenOrgNotFound_ShouldThrowEntityNotFoundException() {
        when(orgRepo.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> organisationService.rename(testUser, 10L, new RenameOrganisationRequest("Renamed Org")));
    }

    @Test
    void rename_WithBlankName_ShouldThrowIllegalArgumentException() {
        when(orgRepo.findById(10L)).thenReturn(Optional.of(testOrg));

        assertThrows(IllegalArgumentException.class, () -> organisationService.rename(testUser, 10L, new RenameOrganisationRequest(" ")));
    }
}
