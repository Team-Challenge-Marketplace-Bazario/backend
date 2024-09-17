package io.teamchallenge.project.bazario.config;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.teamchallenge.project.bazario.entity.User;
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

    public JwtTokenProvider(@Value("${app.jwt_secret}") String jwtSecret,
                            @Value("${app.jwt_duration_s}") long jwtExpirationInSeconds) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationInSeconds = jwtExpirationInSeconds;
    }

    public String generateToken(User user) {
        final var now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtExpirationInSeconds);

        return Jwts.builder()
                .subject(user.getUsername())
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
        Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parse(token);

        //todo: check expiration
        return true;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}
