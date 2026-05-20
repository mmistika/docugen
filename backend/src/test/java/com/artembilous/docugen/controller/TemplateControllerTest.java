package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.TemplateDTO;
import com.artembilous.docugen.dto.TemplateDetailDTO;
import com.artembilous.docugen.dto.TemplateUpdateRequest;
import com.artembilous.docugen.entity.TemplateVersionStatus;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.TemplateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TemplateController.class, properties = "cors.allowed-origins=http://localhost")
class TemplateControllerTest extends BaseControllerTest {

    @MockitoBean
    private TemplateService templateService;

    @Test
    void all_ShouldReturnTemplatesList() throws Exception {
        TemplateDTO dto = new TemplateDTO(100L, "My Template");
        when(templateService.getAll(any(User.class), eq(10L))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/org/10/templates")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("My Template"));
    }

    @Test
    void getForEdit_ShouldReturnTemplateDetails() throws Exception {
        TemplateDetailDTO dto = new TemplateDetailDTO(100L, "My Template", 1, "{}", "content", TemplateVersionStatus.DRAFT);
        when(templateService.getForUpdate(any(User.class), eq(10L), eq(100L))).thenReturn(dto);

        mockMvc.perform(get("/api/org/10/templates/100/edit")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Template"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getActive_ShouldReturnTemplateDetails() throws Exception {
        TemplateDetailDTO dto = new TemplateDetailDTO(100L, "My Template", 1, "{}", "content", TemplateVersionStatus.ACTIVE);
        when(templateService.getActive(any(User.class), eq(10L), eq(100L))).thenReturn(dto);

        mockMvc.perform(get("/api/org/10/templates/100/active")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_ShouldReturnTemplateId() throws Exception {
        TemplateUpdateRequest req = new TemplateUpdateRequest("New Template", "{}", "content");
        when(templateService.update(any(User.class), eq(10L), eq(null), any(TemplateUpdateRequest.class))).thenReturn(200L);

        mockMvc.perform(post("/api/org/10/templates")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(200L));
    }

    @Test
    void update_ShouldReturnTemplateId() throws Exception {
        TemplateUpdateRequest req = new TemplateUpdateRequest("Updated Template", "{}", "content");
        when(templateService.update(any(User.class), eq(10L), eq(100L), any(TemplateUpdateRequest.class))).thenReturn(100L);

        mockMvc.perform(put("/api/org/10/templates/100")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(100L));
    }

    @Test
    void publish_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/org/10/templates/100/publish")
                        .with(authentication(testAuth))
                        .with(csrf()))
                .andExpect(status().isOk());

        Mockito.verify(templateService).publish(any(User.class), eq(10L), eq(100L));
    }
}
