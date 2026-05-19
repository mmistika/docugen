package com.artembilous.docugen.controller;

import com.artembilous.docugen.entity.User;
import com.artembilous.docugen.security.ApiTokenAuthenticationFilter;
import com.artembilous.docugen.security.JwtAuthConverter;
import com.artembilous.docugen.security.RegistrationFilter;
import com.artembilous.docugen.security.UserAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;

public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtAuthConverter jwtAuthConverter;

    @MockitoBean
    protected RegistrationFilter registrationFilter;

    @MockitoBean
    protected ApiTokenAuthenticationFilter apiTokenAuthenticationFilter;

    @MockitoBean
    protected CacheManager cacheManager;

    protected UserAuthenticationToken testAuth;
    protected User testUser;

    @BeforeEach
    protected void setUp() throws Exception {
        Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(registrationFilter).doFilter(any(), any(), any());

        Mockito.doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(apiTokenAuthenticationFilter).doFilter(any(), any(), any());

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setEmail("john@example.com");

        Jwt jwt = Mockito.mock(Jwt.class);
        testAuth = new UserAuthenticationToken(testUser, jwt);
    }
}
