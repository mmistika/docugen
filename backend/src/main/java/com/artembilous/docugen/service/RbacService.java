package com.artembilous.docugen.service;

import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final MembershipRepository membershipRepository;

    public boolean hasPermission(User user, Long orgId, String permission) {
        return membershipRepository.existsPermission(user.getUserId(), orgId, permission);
    }
}