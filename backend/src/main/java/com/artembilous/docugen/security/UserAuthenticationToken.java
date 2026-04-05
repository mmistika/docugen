package com.artembilous.docugen.security;

import com.artembilous.docugen.entity.User;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

@Getter
public class UserAuthenticationToken extends AbstractAuthenticationToken {

    private final User user;
    private final Jwt jwt;

    public UserAuthenticationToken(User user, Jwt jwt) {
        super((Collection<? extends GrantedAuthority>) null);
        this.user = user;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    @Override
    public Object getPrincipal() {
        return user;
    }
}