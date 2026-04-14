package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.CompleteRegistrationRequest;
import com.artembilous.docugen.dto.InviteUserRequest;
import com.artembilous.docugen.dto.MeResponse;
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
        return new MeResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                userService.isFullyRegistered(user)
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
