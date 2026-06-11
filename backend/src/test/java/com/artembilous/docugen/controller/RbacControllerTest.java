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

package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.PermissionDTO;
import com.artembilous.docugen.dto.RoleDTO;
import com.artembilous.docugen.dto.RoleUpdateRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.RbacService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RbacController.class, properties = "cors.allowed-origins=http://localhost")
class RbacControllerTest extends BaseControllerTest {

    @MockitoBean
    private RbacService rbacService;

    @Test
    void permissions_ShouldReturnPermissions() throws Exception {
        PermissionDTO p = new PermissionDTO(1L, "template:view");
        when(rbacService.getPermissions()).thenReturn(Set.of(p));

        mockMvc.perform(get("/api/org/10/rbac/permissions")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("template:view"));
    }

    @Test
    void roles_ShouldReturnRoles() throws Exception {
        RoleDTO role = new RoleDTO("DEVELOPER", Set.of("template:view"));
        when(rbacService.getRoles(any(User.class), eq(10L))).thenReturn(Set.of(role));

        mockMvc.perform(get("/api/org/10/rbac/roles")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("DEVELOPER"))
                .andExpect(jsonPath("$[0].permissions[0]").value("template:view"));
    }

    @Test
    void create_ShouldCallRbacService() throws Exception {
        RoleUpdateRequest req = new RoleUpdateRequest("DEVELOPER", Set.of("template:view"));

        mockMvc.perform(post("/api/org/10/rbac/roles")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Mockito.verify(rbacService).createRole(any(User.class), eq(10L), any(RoleUpdateRequest.class));
    }

    @Test
    void update_ShouldCallRbacService() throws Exception {
        RoleUpdateRequest req = new RoleUpdateRequest("DEVELOPER", Set.of("template:view"));

        mockMvc.perform(put("/api/org/10/rbac/roles")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Mockito.verify(rbacService).updateRole(any(User.class), eq(10L), any(RoleUpdateRequest.class));
    }

    @Test
    void deleteRole_ShouldCallRbacService() throws Exception {
        mockMvc.perform(delete("/api/org/10/rbac/roles/DEVELOPER")
                        .with(authentication(testAuth))
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(rbacService).deleteRole(any(User.class), eq(10L), eq("DEVELOPER"));
    }
}
