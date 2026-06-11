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

import com.artembilous.docugen.entity.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

public class ApiTokenAuthenticationToken extends AbstractAuthenticationToken {

    private final User user;
    private final Long organisationId;
    private final Set<String> permissions;
    private final Long tokenId;
    private final String tokenName;

    public ApiTokenAuthenticationToken(User user, Long organisationId, Set<String> permissions, Long tokenId, String tokenName) {
        super(permissions.stream().map(p -> new SimpleGrantedAuthority("ROLE_" + p)).collect(Collectors.toList()));
        this.user = user;
        this.organisationId = organisationId;
        this.permissions = permissions;
        this.tokenId = tokenId;
        this.tokenName = tokenName;
        setAuthenticated(true);
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public String getTokenName() {
        return tokenName;
    }

    public boolean hasPermission(String permissionName) {
        return permissions.contains(permissionName);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
}
