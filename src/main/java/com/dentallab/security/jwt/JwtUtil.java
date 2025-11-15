package com.dentallab.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JwtUtil
 * - HS256 with >=256-bit key (use strong random secret)
 * - Access vs Refresh tokens separated by "typ" claim
 * - Standard claims: iss, aud, iat, nbf, exp, jti, sub
 * - Small clock skew tolerance when validating
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /* ========= Configurable properties from application.yml ========= */

    @Value("${security.jwt.secret}")
    private String secret; // raw (recommended) or base64—see getSigningKey()

    @Value("${security.jwt.issuer:com.dentallab}")
    private String issuer;

    @Value("${security.jwt.audience:web}")
    private String audience;

    /** Access token lifetime (e.g. PT15M) */
    @Value("${security.jwt.access-ttl:PT15M}")
    private Duration accessTtl;

    /** Refresh token lifetime (e.g. P7D) */
    @Value("${security.jwt.refresh-ttl:P7D}")
    private Duration refreshTtl;

    /** Allowed clock skew during validation */
    @Value("${security.jwt.clock-skew:PT30S}")
    private Duration allowedSkew;

    private final Clock clock = Clock.systemUTC();

    /* ================= Startup validation ================= */

    @PostConstruct
    public void validateSecretStrength() {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 characters (256 bits) for HS256");
        }
    }

    /* ================= Public API used by controllers ================= */

    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authoritiesToStrings(authentication.getAuthorities());
        return buildToken(username, roles, "access", accessTtl);
    }

    public String generateAccessTokenFromUser(UserDetails user) {
        String username = user.getUsername();
        List<String> roles = authoritiesToStrings(user.getAuthorities());
        return buildToken(username, roles, "access", accessTtl);
    }

    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        List<String> roles = authoritiesToStrings(authentication.getAuthorities());
        return buildToken(username, roles, "refresh", refreshTtl);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    /** "typ" claim ("access" | "refresh") */
    public String getType(String token) {
        Object typ = parseClaims(token).get("typ");
        return typ == null ? null : String.valueOf(typ);
    }

    /** Validates signature, expiry, nbf, issuer, audience. */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("❌ Invalid JWT: {}", e.getMessage());
            throw e;
        }
    }

    /* ============================== Internals ============================== */

    private String buildToken(String username, List<String> roles, String type, Duration ttl) {
        Instant now = Instant.now(clock);
        Instant nbf = now.minusSeconds(2); // small backoff for distributed systems
        Instant exp = now.plus(ttl);

        String jti = UUID.randomUUID().toString();

        Map<String, Object> customClaims = Map.of(
                "typ", type,   // "access" or "refresh"
                "roles", roles // for convenience
        );

        return Jwts.builder()
                .setClaims(customClaims)
                .setSubject(username)            // sub
                .setId(jti)                      // jti
                .setIssuer(issuer)               // iss
                .setAudience(audience)           // aud
                .setIssuedAt(Date.from(now))     // iat
                .setNotBefore(Date.from(nbf))    // nbf
                .setExpiration(Date.from(exp))   // exp
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .setAllowedClockSkewSeconds(Math.max(0, (int) allowedSkew.getSeconds()))
                    .build()
                    .parseClaimsJws(token);

            // Defensive check against "none" algorithm or mismatched alg header
            if (!SignatureAlgorithm.HS256.getValue()
                    .equalsIgnoreCase(parsed.getHeader().getAlgorithm())) {
                throw new JwtException("Unexpected JWT algorithm: "
                        + parsed.getHeader().getAlgorithm());
            }
            return parsed.getBody();

        } catch (JwtException | IllegalArgumentException e) {
            throw e; // rethrow so validateToken() can handle
        }
    }

    private SecretKey getSigningKey() {
        /*
         * IMPORTANT:
         *  - If you supply a raw secret string (recommended), we use its UTF-8 bytes.
         *  - Ensure it's >= 32 chars (256 bits) for HS256 strength.
         *  - If you want to use a BASE64 secret, store it with a "base64:" prefix:
         *      security.jwt.secret=base64:AbCdEf...  (then we decode it)
         */
        byte[] keyBytes;
        if (secret.startsWith("base64:")) {
            String b64 = secret.substring("base64:".length());
            keyBytes = java.util.Base64.getDecoder().decode(b64);
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes); // throws if < 256-bit
    }

    private static List<String> authoritiesToStrings(
            Collection<? extends GrantedAuthority> authorities) {
        return authorities == null ? List.of() :
                authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());
    }
}
