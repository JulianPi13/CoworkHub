package com.campus.coworkhub.security;

import com.campus.coworkhub.config.AppConfig;
import com.campus.coworkhub.persistence.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Emite y valida los JWT (HS256) que usa la API de forma stateless. */
@Service
public class JwtService {

    private static final String CLAIM_ROLE = "rol";
    private static final String CLAIM_MEMBER_ID = "miembroId";
    private static final int MIN_SECRET_BYTES = 32;

    private final String secret;
    private final long expirationMinutes;
    private final Clock clock;
    private SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
                      @Qualifier(AppConfig.UTC_CLOCK) Clock clock) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
        this.clock = clock;
    }

    /** Valida la configuración al arrancar: si la clave es corta o la expiración inválida, la app no inicia. */
    @PostConstruct
    void validateConfiguration() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("app.jwt.secret debe tener al menos " + MIN_SECRET_BYTES + " caracteres");
        }
        if (expirationMinutes <= 0) {
            throw new IllegalStateException("app.jwt.expiration-minutes debe ser mayor que 0");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Role role, Long memberId) {
        Instant now = clock.instant();
        var builder = Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(getExpiration())))
                .signWith(key);
        if (memberId != null) {
            builder.claim(CLAIM_MEMBER_ID, memberId);
        }
        return builder.compact();
    }

    /** Devuelve el usuario del token si la firma es válida y no venció; vacío si fue alterado o venció. */
    public Optional<AuthenticatedUser> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
            Number memberId = claims.get(CLAIM_MEMBER_ID, Number.class);
            return Optional.of(new AuthenticatedUser(claims.getSubject(), role,
                    memberId == null ? null : memberId.longValue()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Duration getExpiration() {
        return Duration.ofMinutes(expirationMinutes);
    }
}
