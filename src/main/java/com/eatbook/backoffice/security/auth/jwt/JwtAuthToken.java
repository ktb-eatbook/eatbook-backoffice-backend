package com.eatbook.backoffice.security.auth.jwt;

import com.eatbook.backoffice.security.auth.AuthToken;
import com.eatbook.backoffice.security.error.exception.JwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.eatbook.backoffice.global.response.GlobalErrorCode.*;

@Slf4j
public class JwtAuthToken implements AuthToken<Claims> {
    public static final String AUTHORITIES_KEY = "role";

    @Getter
    private final String token;

    private final Key key;

    public JwtAuthToken(String token, Key key) {
        this.token = token;
        this.key = key;
    }

    public JwtAuthToken(String id, Key key, String role,
                        Map<String, String> claims, Date expiredDate) {
        this.key = key;
        this.token = createJwtToken(id, role, claims, expiredDate)
                .orElseThrow(()-> new JwtTokenException(JWT_MALFORMED));
    }

    @Override
    public boolean validate() throws JwtTokenException {
        return getData() != null;
    }

    @Override
    public Claims getData() {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(JWT_EXPIRED);
        } catch (MalformedJwtException e) {
            throw new JwtTokenException(JWT_MALFORMED);
        } catch (UnsupportedJwtException e) {
            throw new JwtTokenException(JWT_UNSUPPORTED);
        } catch (SignatureException e) {
            throw new JwtTokenException(JWT_INVALID_SIGNATURE);
        } catch (MissingClaimException e) {
            throw new JwtTokenException(JWT_MISSING_CLAIM);
        } catch (IncorrectClaimException e) {
            throw new JwtTokenException(JWT_INCORRECT_CLAIM);
        } catch (PrematureJwtException e) {
            throw new JwtTokenException(JWT_INVALID_FORMAT);
        } catch (JwtException e) {
            throw new JwtTokenException(JWT_UNKNOWN_ERROR);
        } catch (IllegalArgumentException e) {
            throw new JwtTokenException(JWT_INVALID_FORMAT);
        }
    }

    private Optional<String> createJwtToken(String id, String role,
                                            Map<String, String> claimsMap, Date expiredDate) {
        Claims claims = new DefaultClaims(claimsMap);
        claims.put(AUTHORITIES_KEY, role);
        claims.setIssuedAt(new Date());
        return Optional.ofNullable(Jwts.builder()
                .setSubject(id)
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(expiredDate)
                .compact()
        );
    }
}
