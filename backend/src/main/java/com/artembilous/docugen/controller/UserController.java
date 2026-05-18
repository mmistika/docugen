package com.artembilous.docugen.controller;

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
        byte[] imageBytes = decodeBase64Image(req.image());
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
    public void complete(@AuthenticationPrincipal User user, @Valid @RequestBody UpdateProfileRequest req) {
        byte[] imageBytes = decodeBase64Image(req.image());
        userService.updateProfile(user, req.name(), req.surname(), imageBytes);
    }

    private byte[] decodeBase64Image(String base64Image) {
        if (base64Image == null || base64Image.isBlank()) {
            return null;
        }
        String base64Data = base64Image;
        if (base64Data.contains(",")) {
            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
        }
        return java.util.Base64.getDecoder().decode(base64Data);
    }

    @PostMapping("/invite")
    public void invite(@AuthenticationPrincipal User user, @Valid @RequestBody InviteUserRequest req) {
        userService.invite(user, req);
    }
}
