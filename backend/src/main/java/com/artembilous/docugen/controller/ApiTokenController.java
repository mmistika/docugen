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
