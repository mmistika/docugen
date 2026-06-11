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
import com.artembilous.docugen.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final MembershipRepository membershipRepository;

    @Override
    public boolean hasPermission(@NonNull Authentication auth, @NonNull Object targetDomainObject, @NonNull Object permission) {
        if (!(permission instanceof String permissionName)) {
            return false;
        }
        Long orgId = (Long) targetDomainObject;
        return check(auth, orgId, permissionName);
    }

    @Override
    public boolean hasPermission(@NonNull Authentication auth, @NonNull Serializable targetId, @NonNull String targetType, @NonNull Object permission) {
        if (!(permission instanceof String)) {
            return false;
        }
        Long orgId = Long.valueOf(targetId.toString());
        return check(auth, orgId, (String) permission);
    }

    private boolean check(Authentication auth, Long orgId, String permissionName) {
        if (auth instanceof ApiTokenAuthenticationToken apiTokenAuth) {
            return apiTokenAuth.getOrganisationId().equals(orgId) && apiTokenAuth.hasPermission(permissionName);
        }

        if (!(auth instanceof UserAuthenticationToken userAuth)) {
            return false;
        }
        User user = userAuth.getUser();
        return membershipRepository.existsPermission(user.getUserId(), orgId, permissionName);
    }
}