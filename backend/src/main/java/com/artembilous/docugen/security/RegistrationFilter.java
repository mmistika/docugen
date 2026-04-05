package com.artembilous.docugen.security;

import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.exception.RegistrationIncompleteException;
import com.artembilous.docugen.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RegistrationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (auth.getPrincipal() instanceof User user) {
            String path = request.getRequestURI();
            boolean isAllowedPath = path.equals("/api/users/me") ||
                    path.equals("/api/users/complete-registration") ||
                    path.equals("/api/org");

            if (!isAllowedPath && !userService.isFullyRegistered(user)) {
                handlerExceptionResolver.resolveException(request, response, null,
                        new RegistrationIncompleteException("Registration is incomplete"));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}