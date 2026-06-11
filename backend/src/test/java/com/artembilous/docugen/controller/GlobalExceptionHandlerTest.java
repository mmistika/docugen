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

import com.artembilous.docugen.exception.GlobalExceptionHandler;
import com.artembilous.docugen.exception.RegistrationIncompleteException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {GlobalExceptionHandlerTest.ExceptionTestController.class, GlobalExceptionHandler.class}, properties = "cors.allowed-origins=http://localhost")
@Import(GlobalExceptionHandlerTest.ExceptionTestController.class)
class GlobalExceptionHandlerTest extends BaseControllerTest {

    @Test
    void handleNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/test/not-found")
                        .with(authentication(testAuth)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ENTITY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Entity not found message"));
    }

    @Test
    void handleBadRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/test/bad-request")
                        .with(authentication(testAuth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Bad request message"));
    }

    @Test
    void handleConflict_ShouldReturn409() throws Exception {
        mockMvc.perform(get("/api/test/conflict")
                        .with(authentication(testAuth)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Conflict message"));
    }

    @Test
    void handleAccessDenied_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/test/access-denied")
                        .with(authentication(testAuth)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action."));
    }

    @Test
    void handleRegistrationIncomplete_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/test/registration-incomplete")
                        .with(authentication(testAuth)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("REGISTRATION_INCOMPLETE"))
                .andExpect(jsonPath("$.message").value("Registration incomplete message"));
    }

    @RestController
    public static class ExceptionTestController {

        @GetMapping("/api/test/not-found")
        public void notFound() {
            throw new EntityNotFoundException("Entity not found message");
        }

        @GetMapping("/api/test/bad-request")
        public void badRequest() {
            throw new IllegalArgumentException("Bad request message");
        }

        @GetMapping("/api/test/conflict")
        public void conflict() {
            throw new IllegalStateException("Conflict message");
        }

        @GetMapping("/api/test/access-denied")
        public void accessDenied() {
            throw new AccessDeniedException("Access denied message");
        }

        @GetMapping("/api/test/registration-incomplete")
        public void registrationIncomplete() {
            throw new RegistrationIncompleteException("Registration incomplete message");
        }
    }
}
