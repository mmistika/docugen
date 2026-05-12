package com.artembilous.docugen.service;

import com.artembilous.docugen.dto.InviteUserRequest;
import com.artembilous.docugen.entity.*;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.OrganisationRepository;
import com.artembilous.docugen.repository.RoleRepository;
import com.artembilous.docugen.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final OrganisationRepository organisationRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final CacheManager cacheManager;

    @Cacheable(value = "users", key = "#jwt.subject")
    @Transactional
    public User getOrCreate(Jwt jwt) {

        String auth0Id = jwt.getSubject();
        String email = jwt.getClaim("docugen-api/email");

        Optional<User> byAuth0 = userRepository.findByAuth0Id(auth0Id);
        if (byAuth0.isPresent()) {
            return byAuth0.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            user.setAuth0Id(auth0Id);
            return user;
        }

        User user = new User();
        user.setAuth0Id(auth0Id);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Cacheable(value = "registration_status", key = "#user.userId")
    public boolean isFullyRegistered(User user) {
        return user.getName() != null &&
                user.getSurname() != null &&
                membershipRepository.existsByUser(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#user.auth0Id"),
            @CacheEvict(value = "registration_status", key = "#user.userId")
    })
    public void completeRegistration(User user, String name, String surname) {
        user.setName(name);
        user.setSurname(surname);
        userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("hasPermission(#req.orgId(), 'members:manage')")
    public void invite(User inviter, InviteUserRequest req) {
        if (inviter.getEmail().equalsIgnoreCase(req.email().trim())) {
            throw new IllegalArgumentException("You cannot invite yourself to the organisation");
        }
        User user = userRepository.findByEmail(req.email())
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(req.email());
                    u.setAuth0Id(null);
                    return userRepository.save(u);
                });

        Organisation org = organisationRepository.getReferenceById(req.orgId());

        Membership membership = membershipRepository
                .findByUserAndOrganisation(user, org)
                .orElseGet(() -> {
                    Membership m = new Membership();
                    m.setUser(user);
                    m.setOrganisation(org);
                    m.setRoles(new HashSet<>());
                    return membershipRepository.save(m);
                });

        Role role = roleRepository
                .findByNameAndOrganisation(req.role(), org)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        membership.getRoles().add(role);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("orgId", req.orgId());
        metadata.put("userEmail", req.email());
        metadata.put("role", req.role());
        auditService.log(req.orgId(), inviter, AuditEntityType.USER, user.getUserId(), AuditAction.USER_INVITED, metadata);

        if (cacheManager != null && cacheManager.getCache("registration_status") != null) {
            Objects.requireNonNull(cacheManager.getCache("registration_status")).evict(user.getUserId());
        }
    }
}