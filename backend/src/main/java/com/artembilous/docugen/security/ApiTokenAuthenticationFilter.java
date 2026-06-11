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

package com.artembilous.docugen.security;

import com.artembilous.docugen.entity.ApiToken;
import com.artembilous.docugen.entity.Permission;
import com.artembilous.docugen.repository.ApiTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private final ApiTokenRepository apiTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/m2m/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");
        if (apiKey == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Apikey ")) {
                apiKey = authHeader.substring(7);
            }
        }

        if (apiKey != null && apiKey.startsWith("dg_")) {
            String hash = TokenHasher.hash(apiKey);

            Optional<ApiToken> tokenOpt = apiTokenRepository.findByHashWithPermissionsAndOrganisation(hash);
            if (tokenOpt.isPresent()) {
                ApiToken token = tokenOpt.get();
                if (token.isActive() && (token.getExpiresAt() == null || token.getExpiresAt().isAfter(LocalDateTime.now()))) {
                    Set<String> permissions = token.getPermissions().stream()
                            .map(Permission::getName)
                            .collect(Collectors.toSet());

                    ApiTokenAuthenticationToken auth = new ApiTokenAuthenticationToken(
                            null,
                            token.getOrganisation().getOrganisationId(),
                            permissions,
                            token.getTokenId(),
                            token.getName()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
