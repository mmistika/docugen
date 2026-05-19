package com.artembilous.docugen.security;

import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.MembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomPermissionEvaluatorTest {

    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private CustomPermissionEvaluator customPermissionEvaluator;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(42L);
    }

    @Test
    void hasPermission_WhenPermissionIsNotString_ShouldReturnFalse() {
        Authentication auth = mock(Authentication.class);
        assertFalse(customPermissionEvaluator.hasPermission(auth, 1L, 123));
        assertFalse(customPermissionEvaluator.hasPermission(auth, 1L, "org", 123));
    }

    @Test
    void hasPermission_WhenAuthNotSupported_ShouldReturnFalse() {
        Authentication auth = mock(Authentication.class);
        assertFalse(customPermissionEvaluator.hasPermission(auth, 1L, "template:view"));
    }

    @Test
    void hasPermission_WithApiTokenAuth_ShouldReturnTrueIfOrgMatchesAndHasPermission() {
        ApiTokenAuthenticationToken auth = new ApiTokenAuthenticationToken(
                testUser, 1L, Set.of("template:view"), 100L, "test-token"
        );

        assertTrue(customPermissionEvaluator.hasPermission(auth, 1L, "template:view"));
        assertFalse(customPermissionEvaluator.hasPermission(auth, 1L, "template:write"));
        assertFalse(customPermissionEvaluator.hasPermission(auth, 2L, "template:view"));
    }

    @Test
    void hasPermission_WithUserAuth_ShouldQueryMembershipRepository() {
        UserAuthenticationToken auth = mock(UserAuthenticationToken.class);
        when(auth.getUser()).thenReturn(testUser);

        when(membershipRepository.existsPermission(42L, 1L, "template:view")).thenReturn(true);
        when(membershipRepository.existsPermission(42L, 1L, "template:write")).thenReturn(false);

        assertTrue(customPermissionEvaluator.hasPermission(auth, 1L, "template:view"));
        assertFalse(customPermissionEvaluator.hasPermission(auth, 1L, "template:write"));
    }

    @Test
    void hasPermission_WithSerializableTargetId_ShouldParseIdAndCheck() {
        UserAuthenticationToken auth = mock(UserAuthenticationToken.class);
        when(auth.getUser()).thenReturn(testUser);
        when(membershipRepository.existsPermission(42L, 5L, "organisation:manage")).thenReturn(true);

        Serializable targetId = "5";
        assertTrue(customPermissionEvaluator.hasPermission(auth, targetId, "organisation", "organisation:manage"));
    }
}
