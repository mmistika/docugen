package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.ApiTokenDTO;
import com.artembilous.docugen.dto.CreateApiTokenRequest;
import com.artembilous.docugen.dto.CreateApiTokenResponse;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.ApiTokenRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiTokenServiceTest {

    @Mock
    private ApiTokenRepository apiTokenRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private ApiTokenService apiTokenService;

    private User testUser;
    private Organisation testOrg;
    private ApiToken testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        testOrg = new Organisation();
        testOrg.setOrganisationId(10L);
        testOrg.setName("Test Org");

        testToken = new ApiToken();
        testToken.setTokenId(100L);
        testToken.setName("Test Token");
        testToken.setOrganisation(testOrg);
        testToken.setCreatedAt(LocalDateTime.now());
        testToken.setExpiresAt(LocalDateTime.now().plusDays(30));
        testToken.setPermissions(new HashSet<>());
    }

    @Test
    void listTokens_ShouldReturnMappedDtos() {
        when(apiTokenRepository.findByOrganisationOrganisationIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(testToken));

        List<ApiTokenDTO> results = apiTokenService.listTokens(testUser, 10L);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Token", results.getFirst().name());
    }

    @Test
    void createToken_ShouldSaveAndLogAndReturnResponse() {
        CreateApiTokenRequest req = new CreateApiTokenRequest("New Token", Set.of("template:view"), LocalDateTime.now().plusDays(10));

        Permission perm = new Permission();
        perm.setName("template:view");

        when(organisationRepository.getReferenceById(10L)).thenReturn(testOrg);
        when(permissionRepository.findByNameIn(req.permissions())).thenReturn(Set.of(perm));

        CreateApiTokenResponse response = apiTokenService.createToken(testUser, 10L, req);

        assertNotNull(response);
        assertEquals("New Token", response.name());
        assertTrue(response.rawToken().startsWith("dg_"));
        verify(apiTokenRepository, times(1)).save(any(ApiToken.class));
        verify(auditService, times(1)).log(eq(10L), eq(testUser), eq(AuditEntityType.API_TOKEN), any(), eq(AuditAction.API_TOKEN_GENERATED), any());
    }

    @Test
    void deleteToken_WhenTokenExistsAndBelongsToOrg_ShouldDeleteAndLog() {
        when(apiTokenRepository.findById(100L)).thenReturn(Optional.of(testToken));

        apiTokenService.deleteToken(testUser, 10L, 100L);

        verify(apiTokenRepository, times(1)).delete(testToken);
        verify(auditService, times(1)).log(eq(10L), eq(testUser), eq(AuditEntityType.API_TOKEN), eq(100L), eq(AuditAction.API_TOKEN_DELETED), any());
    }

    @Test
    void deleteToken_WhenTokenDoesNotExist_ShouldThrowEntityNotFoundException() {
        when(apiTokenRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> apiTokenService.deleteToken(testUser, 10L, 100L));
    }

    @Test
    void deleteToken_WhenTokenBelongsToDifferentOrg_ShouldThrowIllegalArgumentException() {
        Organisation differentOrg = new Organisation();
        differentOrg.setOrganisationId(20L);
        testToken.setOrganisation(differentOrg);

        when(apiTokenRepository.findById(100L)).thenReturn(Optional.of(testToken));

        assertThrows(IllegalArgumentException.class, () -> apiTokenService.deleteToken(testUser, 10L, 100L));
    }
}
