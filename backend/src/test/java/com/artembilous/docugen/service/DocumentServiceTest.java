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

import com.artembilous.docugen.dto.GenerateDocumentRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.DocumentRepository;
import com.artembilous.docugen.repository.TemplateRepository;
import com.artembilous.docugen.repository.TemplateVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateVersionRepository versionRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PdfService pdfService;

    @Mock
    private AuditService auditService; // Needed for generate_WhenValidRequest_ShouldReturnPdf

    @InjectMocks
    private DocumentService documentService;

    private User testUser;
    private Template testTemplate;
    private TemplateVersion testVersion;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        Organisation org = new Organisation();
        org.setOrganisationId(1L);

        testTemplate = new Template();
        testTemplate.setTemplateId(10L);
        testTemplate.setOrganisation(org);
        testTemplate.setName("Test Template");

        testVersion = new TemplateVersion();
        testVersion.setTemplate(testTemplate);
        testVersion.setVersion(1);
        testVersion.setStatus(TemplateVersionStatus.ACTIVE);
        testVersion.setContent("Hello {name}");
        testVersion.setManifest("{\"fields\":[{\"name\":\"name\",\"type\":\"text\",\"required\":true,\"minLength\":3}]}");
    }

    @Test
    void generate_WhenValidRequest_ShouldReturnPdf() {
        Map<String, String> data = new HashMap<>();
        data.put("name", "Artem");
        GenerateDocumentRequest req = new GenerateDocumentRequest(10L, data, "Doc Name");

        when(templateRepository.findByTemplateIdAndOrganisation_OrganisationId(10L, 1L))
                .thenReturn(Optional.of(testTemplate));
        when(versionRepository.findLatest(10L))
                .thenReturn(Optional.of(testVersion));
        when(pdfService.generateFromHtml(any())).thenReturn(new byte[]{1, 2, 3});

        byte[] pdf = documentService.generate(testUser, 1L, req);

        assertNotNull(pdf);
        assertArrayEquals(new byte[]{1, 2, 3}, pdf);
        verify(documentRepository, times(1)).save(any());
    }

    @Test
    void generate_WhenMissingRequiredField_ShouldThrowIllegalArgumentException() {
        Map<String, String> data = new HashMap<>();
        GenerateDocumentRequest req = new GenerateDocumentRequest(10L, data, "Doc Name");

        when(templateRepository.findByTemplateIdAndOrganisation_OrganisationId(10L, 1L))
                .thenReturn(Optional.of(testTemplate));
        when(versionRepository.findLatest(10L))
                .thenReturn(Optional.of(testVersion));

        assertThrows(IllegalArgumentException.class, () -> documentService.generate(testUser, 1L, req));
    }

    @Test
    void generate_WhenFieldViolatesMinLength_ShouldThrowIllegalArgumentException() {
        Map<String, String> data = new HashMap<>();
        data.put("name", "Ab"); // Only 2 chars, minLength is 3
        GenerateDocumentRequest req = new GenerateDocumentRequest(10L, data, "Doc Name");

        when(templateRepository.findByTemplateIdAndOrganisation_OrganisationId(10L, 1L))
                .thenReturn(Optional.of(testTemplate));
        when(versionRepository.findLatest(10L))
                .thenReturn(Optional.of(testVersion));

        assertThrows(IllegalArgumentException.class, () -> documentService.generate(testUser, 1L, req));
    }
}
