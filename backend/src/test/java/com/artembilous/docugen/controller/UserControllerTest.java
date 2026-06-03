package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.InviteUserRequest;
import com.artembilous.docugen.dto.UpdateProfileRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, properties = "cors.allowed-origins=http://localhost")
class UserControllerTest extends BaseControllerTest {

    @MockitoBean
    private UserService userService;

    @Test
    void me_ShouldReturnMeResponseWithoutImage() throws Exception {
        when(userService.isFullyRegistered(any(User.class))).thenReturn(true);

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.registered").value(true))
                .andExpect(jsonPath("$.image").isEmpty());
    }

    @Test
    void me_ShouldReturnMeResponseWithImage() throws Exception {
        byte[] imageBytes = new byte[]{1, 2, 3};
        testUser.setImage(imageBytes);
        when(userService.isFullyRegistered(any(User.class))).thenReturn(true);

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(testAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.image").value("data:image/png;base64,AQID"));
    }

    @Test
    void updateProfile_ShouldUpdateProfileAndReturnMeResponse() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest("Jane", "Smith", "data:image/png;base64,AQID");
        when(userService.isFullyRegistered(any(User.class))).thenReturn(true);

        mockMvc.perform(put("/api/users/profile")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.surname").value("Smith"))
                .andExpect(jsonPath("$.image").value("data:image/png;base64,AQID"));

        verify(userService).updateProfile(any(User.class), eq("Jane"), eq("Smith"), eq(new byte[]{1, 2, 3}));
    }

    @Test
    void updateProfile_WhenInvalidRequest_ShouldReturnBadRequest() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest("", "", null);

        mockMvc.perform(put("/api/users/profile")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeRegistration_ShouldCompleteRegistration() throws Exception {
        UpdateProfileRequest req = new UpdateProfileRequest("Jane", "Smith", "data:image/png;base64,AQID");
        when(userService.isFullyRegistered(any(User.class))).thenReturn(true);

        mockMvc.perform(post("/api/users/complete-registration")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.surname").value("Smith"))
                .andExpect(jsonPath("$.registered").value(true))
                .andExpect(jsonPath("$.image").value("data:image/png;base64,AQID"));

        verify(userService).updateProfile(any(User.class), eq("Jane"), eq("Smith"), eq(new byte[]{1, 2, 3}));
    }

    @Test
    void invite_ShouldInviteUser() throws Exception {
        InviteUserRequest req = new InviteUserRequest(10L, "jane@example.com", "DEVELOPER");

        mockMvc.perform(post("/api/users/invite")
                        .with(authentication(testAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(userService).invite(any(User.class), any(InviteUserRequest.class));
    }
}
