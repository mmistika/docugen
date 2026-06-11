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

import com.artembilous.docugen.dto.ApiTokenDTO;
import com.artembilous.docugen.dto.CreateApiTokenRequest;
import com.artembilous.docugen.dto.CreateApiTokenResponse;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.ApiTokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
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

@WebMvcTest(controllers = ApiTokenController.class, properties = "cors.allowed-origins=http://localhost")
class ApiTokenControllerTest extends BaseControllerTest {

    @MockitoBean
    private ApiTokenService apiTokenService;

    @Test
    void list_ShouldReturnTokens() throws Exception {
        ApiTokenDTO token = new ApiTokenDTO(100L, "Token 1", LocalDateTime.now(), null, Set.of("template:view"));
        when(apiTokenService.listTokens(any(User.class), eq(10L))).thenReturn(List.of(token));

        mockMvc.perform(get("/api/org/10/settings/tokens")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Token 1"))
                .andExpect(jsonPath("$[0].permissions[0]").value("template:view"));
    }

    @Test
    void create_ShouldReturnCreatedToken() throws Exception {
        CreateApiTokenRequest req = new CreateApiTokenRequest("New Token", Set.of("template:view"), null);
        CreateApiTokenResponse response = new CreateApiTokenResponse(100L, "New Token", "dg_12345", LocalDateTime.now(), null, Set.of("template:view"));

        when(apiTokenService.createToken(any(User.class), eq(10L), any(CreateApiTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/org/10/settings/tokens")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawToken").value("dg_12345"))
                .andExpect(jsonPath("$.name").value("New Token"));
    }

    @Test
    void deleteToken_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/org/10/settings/tokens/100")
                        .with(authentication(testAuth))
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(apiTokenService).deleteToken(any(User.class), eq(10L), eq(100L));
    }
}
