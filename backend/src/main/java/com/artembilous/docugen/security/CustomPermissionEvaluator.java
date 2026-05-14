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