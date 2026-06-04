package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.DocumentDTO;
import com.artembilous.docugen.dto.GenerateDocumentRequest;
import com.artembilous.docugen.entity.DocumentStatus;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DocumentController.class, properties = "cors.allowed-origins=http://localhost")
class DocumentControllerTest extends BaseControllerTest {

    @MockitoBean
    private DocumentService documentService;

    @Test
    void list_ShouldReturnDocuments() throws Exception {
        DocumentDTO dto = new DocumentDTO(1L, "Test Doc", DocumentStatus.DRAFT, "Template A", LocalDateTime.now());
        PageImpl<DocumentDTO> page = new PageImpl<>(List.of(dto));
        when(documentService.list(any(User.class), eq(10L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/org/10/documents")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].name").value("Test Doc"))
                .andExpect(jsonPath("$.content[0].status").value("DRAFT"))
                .andExpect(jsonPath("$.content[0].templateName").value("Template A"));
    }

    @Test
    void generate_ShouldReturnPdfFile() throws Exception {
        GenerateDocumentRequest req = new GenerateDocumentRequest(100L, Map.of("key", "val"), "Generated Doc");
        byte[] pdfContent = new byte[]{37, 80, 68, 70}; // ASCII: %PDF
        when(documentService.generate(any(User.class), eq(10L), any(GenerateDocumentRequest.class))).thenReturn(pdfContent);

        mockMvc.perform(post("/api/org/10/documents/generate")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=document.pdf"))
                .andExpect(content().bytes(pdfContent));
    }

    @Test
    void finalise_ShouldCallFinaliseOnService() throws Exception {
        mockMvc.perform(patch("/api/org/10/documents/1/finalise")
                        .with(authentication(testAuth))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(documentService).finalise(any(User.class), eq(10L), eq(1L));
    }

    @Test
    void revert_ShouldCallRevertOnService() throws Exception {
        mockMvc.perform(patch("/api/org/10/documents/1/revert")
                        .with(authentication(testAuth))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(documentService).revert(any(User.class), eq(10L), eq(1L));
    }

    @Test
    void get_ShouldReturnPdfFile() throws Exception {
        byte[] pdfContent = new byte[]{37, 80, 68, 70}; // ASCII: %PDF
        when(documentService.view(any(User.class), eq(10L), eq(1L))).thenReturn(pdfContent);

        mockMvc.perform(get("/api/org/10/documents/1")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=document.pdf"))
                .andExpect(content().bytes(pdfContent));
    }
}
