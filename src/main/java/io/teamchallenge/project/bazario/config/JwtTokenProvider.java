package io.teamchallenge.project.bazario.config;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String jwtSecret;
    private final long jwtExpirationInSeconds;

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);


    public JwtTokenProvider(@Value("${app.jwt_secret}") String jwtSecret,
                            @Value("${app.jwt_duration_s}") long jwtExpirationInSeconds) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationInSeconds = jwtExpirationInSeconds;
    }

    public String generateToken(String username) {
        final var now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtExpirationInSeconds);

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key())
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parse(token);

            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token");
        } catch (SecurityException e) {
            log.warn("Security exception");
        } catch (IllegalArgumentException e) {
            log.warn("Illegal JWT token");
        }

        return false;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
