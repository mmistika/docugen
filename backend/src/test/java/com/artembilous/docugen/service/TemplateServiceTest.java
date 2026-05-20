package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.TemplateDTO;
import com.artembilous.docugen.dto.TemplateDetailDTO;
import com.artembilous.docugen.dto.TemplateUpdateRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.TemplateRepository;
import com.artembilous.docugen.repository.TemplateVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateVersionRepository versionRepository;

    @Mock
    private AuditService auditService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TemplateService templateService;

    private User testUser;
    private Template testTemplate;
    private TemplateVersion testVersion;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        Organisation org = new Organisation();
        org.setOrganisationId(10L);

        testTemplate = new Template();
        testTemplate.setTemplateId(100L);
        testTemplate.setName("Test Template");
        testTemplate.setOrganisation(org);

        testVersion = new TemplateVersion();
        testVersion.setTemplate(testTemplate);
        testVersion.setVersion(1);
        testVersion.setManifest("{\"fields\": []}");
        testVersion.setContent("Hello World");
        testVersion.setStatus(TemplateVersionStatus.DRAFT);
    }

    @Test
    void getAll_ShouldReturnTemplates() {
        TemplateDTO dto = new TemplateDTO(100L, "Test Template");
        when(templateRepository.findAllByOrg(10L)).thenReturn(List.of(dto));

        List<TemplateDTO> result = templateService.getAll(testUser, 10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Template", result.get(0).name());
    }

    @Test
    void getForUpdate_WhenTemplateAndVersionExist_ShouldReturnDetail() {
        when(templateRepository.findByTemplateIdAndOrganisation_OrganisationId(100L, 10L))
                .thenReturn(Optional.of(testTemplate));
        when(versionRepository.findLatest(100L)).thenReturn(Optional.of(testVersion));

        TemplateDetailDTO result = templateService.getForUpdate(testUser, 10L, 100L);

        assertNotNull(result);
        assertEquals("Test Template", result.name());
        assertEquals(1, result.version());
    }

    @Test
    void getActive_WhenActiveVersionExists_ShouldReturnDetail() {
        testVersion.setStatus(TemplateVersionStatus.ACTIVE);
        when(templateRepository.findByTemplateIdAndOrganisation_OrganisationId(100L, 10L))
                .thenReturn(Optional.of(testTemplate));
        when(versionRepository.findByTemplateTemplateIdAndStatus(100L, TemplateVersionStatus.ACTIVE))
                .thenReturn(Optional.of(testVersion));

        TemplateDetailDTO result = templateService.getActive(testUser, 10L, 100L);

        assertNotNull(result);
        assertEquals(TemplateVersionStatus.ACTIVE, result.status());
    }

    @Test
    void update_WithValidNewTemplate_ShouldSaveAndLog() {
        TemplateUpdateRequest req = new TemplateUpdateRequest(
                "New Template",
                "{\"fields\": [{\"name\": \"username\", \"type\": \"text\"}]}",
                "Hello {username}"
        );

        when(templateRepository.save(any(Template.class))).thenAnswer(invocation -> {
            Template t = invocation.getArgument(0);
            t.setTemplateId(200L);
            return t;
        });
        when(versionRepository.findByTemplateTemplateIdAndStatus(any(), eq(TemplateVersionStatus.DRAFT)))
                .thenReturn(Optional.empty());
        when(versionRepository.findLatest(any())).thenReturn(Optional.empty());

        Long newId = templateService.update(testUser, 10L, null, req);

        assertEquals(200L, newId);
        verify(versionRepository, times(1)).save(any(TemplateVersion.class));
        verify(auditService, times(1)).log(eq(10L), eq(testUser), eq(AuditEntityType.TEMPLATE), eq(200L), eq(AuditAction.TEMPLATE_CREATED), any());
    }

    @Test
    void update_WithInvalidJsonManifest_ShouldThrowIllegalArgumentException() {
        TemplateUpdateRequest req = new TemplateUpdateRequest(
                "Bad Manifest",
                "invalid-json",
                "content"
        );

        assertThrows(IllegalArgumentException.class, () -> templateService.update(testUser, 10L, null, req));
    }

    @Test
    void update_WithUndeclaredPlaceholderInContent_ShouldThrowIllegalArgumentException() {
        TemplateUpdateRequest req = new TemplateUpdateRequest(
                "Template",
                "{\"fields\": []}",
                "Hello {missingField}"
        );

        assertThrows(IllegalArgumentException.class, () -> templateService.update(testUser, 10L, null, req));
    }

    @Test
    void publish_WhenDraftExists_ShouldPromoteToActiveAndRetireOldActive() {
        TemplateVersion activeVersion = new TemplateVersion();
        activeVersion.setStatus(TemplateVersionStatus.ACTIVE);
        activeVersion.setVersion(1);

        TemplateVersion draftVersion = new TemplateVersion();
        draftVersion.setStatus(TemplateVersionStatus.DRAFT);
        draftVersion.setVersion(2);

        when(templateRepository.findByTemplateIdAndOrganisation_OrganisationId(100L, 10L))
                .thenReturn(Optional.of(testTemplate));
        when(versionRepository.findByTemplateTemplateIdAndStatus(100L, TemplateVersionStatus.DRAFT))
                .thenReturn(Optional.of(draftVersion));
        when(versionRepository.findByTemplateTemplateIdAndStatus(100L, TemplateVersionStatus.ACTIVE))
                .thenReturn(Optional.of(activeVersion));

        templateService.publish(testUser, 10L, 100L);

        assertEquals(TemplateVersionStatus.RETIRED, activeVersion.getStatus());
        assertEquals(TemplateVersionStatus.ACTIVE, draftVersion.getStatus());
        verify(auditService, times(1)).log(eq(10L), eq(testUser), eq(AuditEntityType.TEMPLATE), eq(100L), eq(AuditAction.TEMPLATE_PUBLISHED), any());
    }
}
