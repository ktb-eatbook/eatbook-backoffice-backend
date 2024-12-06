package com.eatbook.backoffice.security.auth.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Setter
public class CustomPasswordAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private String id;
    private String role;
    private String name;

    public CustomPasswordAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public CustomPasswordAuthenticationToken(
            Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities
    ) {
        super(principal, credentials, authorities);
    }
}
