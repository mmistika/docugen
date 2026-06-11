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

import com.artembilous.docugen.dto.AuditLogDTO;
import com.artembilous.docugen.entity.AuditEntityType;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuditController.class, properties = "cors.allowed-origins=http://localhost")
class AuditControllerTest extends BaseControllerTest {

    @MockitoBean
    private AuditService auditService;

    @Test
    void get_ShouldReturnPagedAuditLogs() throws Exception {
        AuditLogDTO dto = new AuditLogDTO(1L, "TEMPLATE_CREATED", AuditEntityType.TEMPLATE, 100L, 1L, "john@example.com", "John Doe", false, "{}", LocalDateTime.now());
        PageImpl<AuditLogDTO> page = new PageImpl<>(List.of(dto));

        when(auditService.getLogs(any(User.class), eq(10L), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/org/10/audit")
                        .with(authentication(testAuth))
                        .param("from", "2026-06-01T00:00:00Z")
                        .param("to", "2026-06-03T00:00:00Z")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("TEMPLATE_CREATED"))
                .andExpect(jsonPath("$.content[0].userName").value("John Doe"));
    }
}
