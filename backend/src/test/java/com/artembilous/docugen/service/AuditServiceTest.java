package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.AuditLogDTO;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.ApiTokenRepository;
import com.artembilous.docugen.repository.AuditLogRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.security.ApiTokenAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private ApiTokenRepository apiTokenRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditService auditService;

    private User testUser;
    private Organisation testOrg;

    @BeforeEach
    void setUp() throws Exception {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setEmail("john.doe@example.com");

        testOrg = new Organisation();
        testOrg.setOrganisationId(10L);

        // Inject self for async log call
        java.lang.reflect.Field selfField = AuditService.class.getDeclaredField("self");
        selfField.setAccessible(true);
        selfField.set(auditService, auditService);

        SecurityContextHolder.clearContext();
    }

    @Test
    void log_WithUserActor_ShouldSaveAuditLogForUser() {
        when(organisationRepository.getReferenceById(10L)).thenReturn(testOrg);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"value\"}");

        auditService.log(10L, testUser, AuditEntityType.TEMPLATE, 100L, AuditAction.TEMPLATE_CREATED, Map.of("key", "value"));

        verify(auditRepository, times(1)).save(argThat(log -> {
            assertEquals(testOrg, log.getOrganisation());
            assertEquals(testUser, log.getUser());
            assertNull(log.getApiToken());
            assertEquals(AuditEntityType.TEMPLATE, log.getEntityType());
            assertEquals(100L, log.getEntityId());
            assertEquals(AuditAction.TEMPLATE_CREATED, log.getAction());
            assertEquals("{\"key\":\"value\"}", log.getMetadata());
            return true;
        }));
    }

    @Test
    void log_WithNoActorAndApiTokenAuth_ShouldSaveAuditLogForToken() {
        ApiTokenAuthenticationToken auth = new ApiTokenAuthenticationToken(
                null, 10L, Set.of(), 500L, "my-api-token"
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        ApiToken token = new ApiToken();
        token.setTokenId(500L);

        when(organisationRepository.getReferenceById(10L)).thenReturn(testOrg);
        when(apiTokenRepository.getReferenceById(500L)).thenReturn(token);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        auditService.log(10L, null, AuditEntityType.DOCUMENT, 200L, AuditAction.DOCUMENT_GENERATED, Map.of());

        verify(auditRepository, times(1)).save(argThat(log -> {
            assertEquals(testOrg, log.getOrganisation());
            assertNull(log.getUser());
            assertEquals(token, log.getApiToken());
            assertEquals("my-api-token", log.getTokenName());
            assertEquals(AuditEntityType.DOCUMENT, log.getEntityType());
            assertEquals(200L, log.getEntityId());
            assertEquals(AuditAction.DOCUMENT_GENERATED, log.getAction());
            return true;
        }));
    }

    @Test
    void getLogs_ShouldReturnMappedDtos() {
        AuditLog log1 = new AuditLog();
        log1.setLogId(1L);
        log1.setAction(AuditAction.TEMPLATE_CREATED);
        log1.setEntityType(AuditEntityType.TEMPLATE);
        log1.setEntityId(100L);
        log1.setUser(testUser);
        log1.setTimestamp(LocalDateTime.now());
        log1.setMetadata("{}");

        Page<AuditLog> page = new PageImpl<>(List.of(log1));
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        when(auditRepository.findAll(eq(10L), any(), any(), eq(pageable))).thenReturn(page);

        Page<AuditLogDTO> result = auditService.getLogs(testUser, 10L, now.minusDays(1), now, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        AuditLogDTO dto = result.getContent().getFirst();
        assertEquals(1L, dto.id());
        assertEquals("TEMPLATE_CREATED", dto.action());
        assertEquals("John Doe", dto.userName());
        assertEquals("john.doe@example.com", dto.userEmail());
        assertFalse(dto.isToken());
    }
}
