package net.awords.agriecombackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private final SecretKey key;
    private final long expirationMs;
    private final String cookieName;

    public JwtUtil(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration}") long expirationMs,
            @Value("${security.jwt.cookie-name}") String cookieName
    ) {
        this.key = buildSigningKey(secret);
        this.expirationMs = expirationMs;
        this.cookieName = cookieName;
    }

    public String generateToken(String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getCookieName() { return cookieName; }

    private SecretKey buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("security.jwt.secret must not be blank");
        }

        byte[] keyBytes = secret.trim().getBytes(StandardCharsets.UTF_8);

        if (looksLikeBase64(secret)) {
            try {
                keyBytes = Decoders.BASE64.decode(secret.trim());
            } catch (IllegalArgumentException ex) {
                log.warn("Provided JWT secret looks like Base64 but failed to decode. Falling back to UTF-8 bytes.");
            }
        }

        if (keyBytes.length < 32) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                keyBytes = Arrays.copyOf(digest.digest(keyBytes), 32);
                log.info("security.jwt.secret was shorter than 32 bytes; applying SHA-256 to strengthen it.");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 MessageDigest not available", e);
            }
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean looksLikeBase64(String value) {
        String trimmed = value.trim();
        if (trimmed.length() < 16) {
            return false;
        }
        if (trimmed.length() % 4 != 0) {
            return false;
        }
        return trimmed.matches("[A-Za-z0-9+/=]+");
    }
}
