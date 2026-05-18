package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.CompleteRegistrationRequest;
import com.artembilous.docugen.dto.InviteUserRequest;
import com.artembilous.docugen.dto.MeResponse;
import com.artembilous.docugen.dto.UpdateProfileRequest;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal User user) {
        String base64Image = null;
        if (user.getImage() != null && user.getImage().length > 0) {
            base64Image = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(user.getImage());
        }
        return new MeResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                userService.isFullyRegistered(user),
                base64Image
        );
    }

    @PutMapping("/profile")
    public MeResponse updateProfile(@AuthenticationPrincipal User user, @Valid @RequestBody UpdateProfileRequest req) {
        byte[] imageBytes = null;
        if (req.image() != null && !req.image().isBlank()) {
            String base64Data = req.image();
            if (base64Data.contains(",")) {
                base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
            }
            imageBytes = java.util.Base64.getDecoder().decode(base64Data);
        }
        userService.updateProfile(user, req.name(), req.surname(), imageBytes);

        String newBase64Image = null;
        if (imageBytes != null && imageBytes.length > 0) {
            newBase64Image = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
        }
        return new MeResponse(
                user.getUserId(),
                user.getEmail(),
                req.name(),
                req.surname(),
                userService.isFullyRegistered(user),
                newBase64Image
        );
    }

    @PostMapping("/complete-registration")
    public void complete(@AuthenticationPrincipal User user, @Valid @RequestBody CompleteRegistrationRequest req) {
        userService.completeRegistration(user, req.name(), req.surname());
    }

    @PostMapping("/invite")
    public void invite(@AuthenticationPrincipal User user, @Valid @RequestBody InviteUserRequest req) {
        userService.invite(user, req);
    }
}
