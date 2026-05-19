package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.*;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.OrganisationService;
import com.artembilous.docugen.service.RbacService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrganisationController.class, properties = "cors.allowed-origins=http://localhost")
class OrganisationControllerTest extends BaseControllerTest {

    @MockitoBean
    private OrganisationService orgService;

    @MockitoBean
    private RbacService rbacService;

    @Test
    void my_ShouldReturnUserOrganisations() throws Exception {
        OrganisationDTO dto = new OrganisationDTO(10L, "Test Org", 2L);
        when(orgService.getUserOrganisations(any(User.class))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/org/my")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Org"))
                .andExpect(jsonPath("$[0].memberCount").value(2));
    }

    @Test
    void create_WhenNameAndSurnameRegistered_ShouldCreateOrg() throws Exception {
        CreateOrganisationRequest req = new CreateOrganisationRequest("New Org");

        mockMvc.perform(post("/api/org")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Mockito.verify(orgService).createOrganisation(any(User.class), eq("New Org"));
    }

    @Test
    void create_WhenRegistrationIncomplete_ShouldThrowRegistrationIncompleteException() throws Exception {
        testUser.setName(null);
        testUser.setSurname(null);
        CreateOrganisationRequest req = new CreateOrganisationRequest("New Org");

        mockMvc.perform(post("/api/org")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMembers_ShouldReturnMemberList() throws Exception {
        MemberDTO dto = new MemberDTO(200L, "Jane", "Smith", "jane@smith.com", List.of("DEVELOPER"), null);
        when(orgService.getOrganisationMembers(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/org/10/members")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Jane"))
                .andExpect(jsonPath("$[0].email").value("jane@smith.com"));
    }

    @Test
    void updateRoles_ShouldCallRbacService() throws Exception {
        UpdateMemberRolesRequest req = new UpdateMemberRolesRequest(Set.of("DEVELOPER"));

        mockMvc.perform(put("/api/org/10/members/200/roles")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Mockito.verify(rbacService).updateMemberRoles(any(User.class), eq(10L), eq(200L), any(UpdateMemberRolesRequest.class));
    }

    @Test
    void rename_ShouldCallRename() throws Exception {
        RenameOrganisationRequest req = new RenameOrganisationRequest("Renamed Org");

        mockMvc.perform(patch("/api/org/10")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Mockito.verify(orgService).rename(any(User.class), eq(10L), any(RenameOrganisationRequest.class));
    }
}
