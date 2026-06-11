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

import com.artembilous.docugen.dto.InviteUserRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@docugen.com");
        testUser.setAuth0Id("auth0|12345");
    }

    @Test
    void getOrCreate_WhenUserExistsByAuth0Id_ShouldReturnUser() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|12345");
        when(jwt.getClaim("docugen-api/email")).thenReturn("test@docugen.com");
        when(userRepository.findByAuth0Id("auth0|12345")).thenReturn(Optional.of(testUser));

        User result = userService.getOrCreate(jwt);

        assertNotNull(result);
        assertEquals("test@docugen.com", result.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOrCreate_WhenUserExistsByEmailOnly_ShouldUpdateAuth0IdAndReturnUser() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("auth0|12345");
        when(jwt.getClaim("docugen-api/email")).thenReturn("test@docugen.com");
        when(userRepository.findByAuth0Id("auth0|12345")).thenReturn(Optional.empty());

        User emailUser = new User();
        emailUser.setEmail("test@docugen.com");
        emailUser.setAuth0Id(null);
        when(userRepository.findByEmail("test@docugen.com")).thenReturn(Optional.of(emailUser));
        User result = userService.getOrCreate(jwt);

        assertNotNull(result);
        assertEquals("auth0|12345", result.getAuth0Id());
    }

    @Test
    void invite_WhenInvitingSelf_ShouldThrowIllegalArgumentException() {
        InviteUserRequest req = new InviteUserRequest(1L, "test@docugen.com", "ADMIN");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.invite(testUser, req)
        );

        assertEquals("You cannot invite yourself to the organisation", exception.getMessage());
    }
}
