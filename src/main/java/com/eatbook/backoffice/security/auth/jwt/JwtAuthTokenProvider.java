package com.eatbook.backoffice.security.auth.jwt;

import com.eatbook.backoffice.security.auth.AuthTokenProvider;
import com.eatbook.backoffice.security.error.exception.JwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Component
public class JwtAuthTokenProvider implements AuthTokenProvider<JwtAuthToken> {

    @Value("${jwt.access-token.expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshExpiration;

    @Value("${jwt.access-token.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public int getRefreshTokenExpiration() {
        return Math.toIntExact(refreshExpiration);
    }

    public int getAccessTokenExpiration() {
        return Math.toIntExact(accessExpiration);
    }

    @Override
    public JwtAuthToken createAuthToken(String id, String role, Map<String, String> claims) {
        Date expiredDate = Date.from(
                LocalDateTime.now()
                        .plusSeconds(accessExpiration)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        return new JwtAuthToken(id, key, role, claims, expiredDate);
    }

    @Override
    public JwtAuthToken convertAuthToken(String token) {
        return new JwtAuthToken(token, key);
    }

    @Override
    public Authentication getAuthentication(JwtAuthToken authToken) {
        if (authToken.validate()) {
            Claims claims = authToken.getData();
            Collection<? extends GrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority(claims.get(JwtAuthToken.AUTHORITIES_KEY, String.class))
            );
            User principal = new User(claims.getSubject(), "", authorities);
            return new UsernamePasswordAuthenticationToken(principal, authToken, authorities);
        } else {
            throw new JwtException("JWT 토큰이 적절하지 않습니다.");
        }
    }

    public JwtAuthToken createRefreshToken(String id) {
        Date expiredDate = Date.from(
                LocalDateTime.now()
                        .plusSeconds(refreshExpiration)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        Map<String, String> claims = Map.of("id", id);
        return new JwtAuthToken(id, key, "ROLE_REFRESH", claims, expiredDate);
    }

    public Claims getClaimsFromExpiredToken(String token) throws JwtTokenException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}