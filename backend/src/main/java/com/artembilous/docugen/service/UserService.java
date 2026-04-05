package com.artembilous.docugen.service;

import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.repository.MembershipRepository;
import com.artembilous.docugen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public User getOrCreate(Jwt jwt) {

        String auth0Id = jwt.getSubject();
        String email = jwt.getClaim("docugen-api/email");

        return userRepository.findByAuth0Id(auth0Id)
                .orElseGet(() -> {
                    User user = new User();
                    user.setAuth0Id(auth0Id);
                    user.setEmail(email);
                    return userRepository.save(user);
                });
    }

    public boolean isFullyRegistered(User user) {
        return user.getName() != null &&
                user.getSurname() != null &&
                membershipRepository.existsByUser(user);
    }

    public void completeRegistration(User user, String name, String surname) {
        user.setName(name);
        user.setSurname(surname);
        userRepository.save(user);
    }
}