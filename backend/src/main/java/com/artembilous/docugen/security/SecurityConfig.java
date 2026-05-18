package com.artembilous.docugen.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthConverter jwtAuthConverter,
            RegistrationFilter registrationFilter,
            ApiTokenAuthenticationFilter apiTokenAuthenticationFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/**").access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(
                                        authentication.get() instanceof UserAuthenticationToken
                                )
                        )
                        .requestMatchers("/m2m/**").access((authentication, context) ->
                                new org.springframework.security.authorization.AuthorizationDecision(
                                        authentication.get() instanceof ApiTokenAuthenticationToken
                                )
                        )
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt( jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                )
                .addFilterBefore(apiTokenAuthenticationFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(registrationFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}