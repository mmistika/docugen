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

import com.artembilous.docugen.dto.ApiTokenDTO;
import com.artembilous.docugen.dto.CreateApiTokenRequest;
import com.artembilous.docugen.dto.CreateApiTokenResponse;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.ApiTokenRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.repository.PermissionRepository;
import com.artembilous.docugen.security.TokenHasher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiTokenService {

    private static final SecureRandom secureRandom = new SecureRandom();
    private final ApiTokenRepository apiTokenRepository;
    private final OrganisationRepository organisationRepository;
    private final PermissionRepository permissionRepository;
    private final AuditService auditService;

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public List<ApiTokenDTO> listTokens(User user, Long orgId) {
        return apiTokenRepository.findByOrganisationOrganisationIdOrderByCreatedAtDesc(orgId).stream()
                .map(t -> new ApiTokenDTO(
                        t.getTokenId(),
                        t.getName(),
                        t.getCreatedAt(),
                        t.getExpiresAt(),
                        t.getPermissions().stream().map(Permission::getName).collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public CreateApiTokenResponse createToken(User user, Long orgId, CreateApiTokenRequest req) {

        // Generate raw token
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken = "dg_" + HexFormat.of().formatHex(bytes);
        String hash = TokenHasher.hash(rawToken);

        ApiToken token = new ApiToken();
        token.setName(req.name());
        token.setHash(hash);
        token.setOrganisation(organisationRepository.getReferenceById(orgId));
        token.setExpiresAt(req.expiresAt());

        Set<Permission> permissions = permissionRepository.findByNameIn(req.permissions());
        token.setPermissions(permissions);

        apiTokenRepository.save(token);

        auditService.log(
                orgId,
                user,
                AuditEntityType.API_TOKEN,
                token.getTokenId(),
                AuditAction.API_TOKEN_GENERATED,
                Map.of("name", token.getName(), "permissions", req.permissions())
        );

        return new CreateApiTokenResponse(
                token.getTokenId(),
                token.getName(),
                rawToken,
                token.getCreatedAt(),
                token.getExpiresAt(),
                req.permissions()
        );
    }

    @Transactional
    @PreAuthorize("hasPermission(#orgId, 'organisation:manage')")
    public void deleteToken(User user, Long orgId, Long tokenId) {
        ApiToken token = apiTokenRepository.findById(tokenId)
                .orElseThrow(() -> new EntityNotFoundException("Token not found"));

        if (!token.getOrganisation().getOrganisationId().equals(orgId)) {
            throw new IllegalArgumentException("Token does not belong to this organisation");
        }

        auditService.log(
                orgId,
                user,
                AuditEntityType.API_TOKEN,
                token.getTokenId(),
                AuditAction.API_TOKEN_DELETED,
                Map.of("name", token.getName())
        );

        apiTokenRepository.delete(token);
    }
}
