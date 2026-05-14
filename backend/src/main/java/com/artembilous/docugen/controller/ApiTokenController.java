package com.artembilous.docugen.controller;

import com.artembilous.docugen.dto.ApiTokenDTO;
import com.artembilous.docugen.dto.CreateApiTokenRequest;
import com.artembilous.docugen.dto.CreateApiTokenResponse;
import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.service.ApiTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org/{orgId}/settings/tokens")
@RequiredArgsConstructor
public class ApiTokenController {

    private final ApiTokenService apiTokenService;

    @GetMapping
    public List<ApiTokenDTO> list(@AuthenticationPrincipal User user, @PathVariable Long orgId) {
        return apiTokenService.listTokens(user, orgId);
    }

    @PostMapping
    public CreateApiTokenResponse create(
            @AuthenticationPrincipal User user,
            @PathVariable Long orgId,
            @Valid @RequestBody CreateApiTokenRequest req
    ) {
        return apiTokenService.createToken(user, orgId, req);
    }

    @DeleteMapping("/{tokenId}")
    public void delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long orgId,
            @PathVariable Long tokenId
    ) {
        apiTokenService.deleteToken(user, orgId, tokenId);
    }
}
