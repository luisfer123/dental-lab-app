package com.dentallab.security.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dentallab.persistence.entity.RefreshTokenEntity;
import com.dentallab.persistence.repository.RefreshTokenRepository;
import com.dentallab.persistence.repository.UserAccountRepository;
import com.dentallab.security.jwt.JwtUtil;
import com.dentallab.security.model.dto.AuthResponse;
import com.dentallab.security.model.dto.LoginRequest;
import com.dentallab.security.model.dto.MessageResponse;
import com.dentallab.security.model.dto.RefreshResponse;
import com.dentallab.security.service.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthController
 * Handles login, token refresh (rotation), and logout endpoints.
 * 
 * - Issues JWT access & refresh tokens
 * - Persists refresh tokens in DB (JTI, expiry, revoked)
 * - Uses HttpOnly cookies for refresh tokens (SameSite=None for cross-site SPA)
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountRepository userAccountRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          CustomUserDetailsService userDetailsService,
                          RefreshTokenRepository refreshTokenRepository,
                          UserAccountRepository userAccountRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userAccountRepository = userAccountRepository;
    }

    // ============================================================
    // LOGIN
    // ============================================================
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest,
                                              HttpServletResponse response) {
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var user = userAccountRepository
                .findByUsernameOrEmailAndEnabledTrue(loginRequest.username(), loginRequest.username())
                .orElseThrow(() -> new RuntimeException("User not found or not enabled"));

        // Generate access and refresh tokens
        String accessToken = jwtUtil.generateAccessToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);

        // Extract metadata
        String jti = jwtUtil.getJti(refreshToken);
        Date expiry = jwtUtil.getExpiration(refreshToken);

        // Persist refresh token
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setToken(refreshToken);
        entity.setTokenId(jti);
        entity.setExpiryDate(expiry.toInstant());
        entity.setRevoked(false);
        refreshTokenRepository.save(entity);

        // Set secure HttpOnly cookie with refresh token
        setRefreshCookie(response, refreshToken, expiry);

        List<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        log.debug("User '{}' logged in successfully with roles {}", user.getUsername(), roles);
        return ResponseEntity.ok(new AuthResponse(accessToken, roles));
    }

    // ============================================================
    // REFRESH (rotate refresh token)
    // ============================================================
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Missing or invalid refresh token"));
        }

        // Must be a refresh token
        String type = jwtUtil.getType(refreshToken);
        if (!"refresh".equals(type)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid token type"));
        }

        String jti = jwtUtil.getJti(refreshToken);
        String username = jwtUtil.extractUsername(refreshToken);

        var tokenEntity = refreshTokenRepository.findByTokenId(jti).orElse(null);
        if (tokenEntity == null) {
            log.warn("Refresh token JTI {} not found in DB", jti);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid refresh token"));
        }

        // existing validation
        if (tokenEntity.isRevoked()
                || tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Refresh token revoked or expired"));
        }

        // Rotate refresh token
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        var userDetails = userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtUtil.generateAccessTokenFromUser(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(
                new UsernamePasswordAuthenticationToken(username, null, userDetails.getAuthorities())
        );

        // Save rotated refresh token
        String newJti = jwtUtil.getJti(newRefreshToken);
        Date newExpiry = jwtUtil.getExpiration(newRefreshToken);

        RefreshTokenEntity newEntity = new RefreshTokenEntity();
        newEntity.setUser(tokenEntity.getUser());
        newEntity.setToken(newRefreshToken);
        newEntity.setTokenId(newJti);
        newEntity.setExpiryDate(newExpiry.toInstant());
        newEntity.setRevoked(false);
        refreshTokenRepository.save(newEntity);

        setRefreshCookie(response, newRefreshToken, newExpiry);

        return ResponseEntity.ok(new RefreshResponse(newAccessToken));
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<MessageResponse> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken != null && jwtUtil.validateToken(refreshToken)
                && "refresh".equals(jwtUtil.getType(refreshToken))) {

            String jti = jwtUtil.getJti(refreshToken);
            refreshTokenRepository.findByTokenId(jti)
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                        log.debug("Refresh token revoked for user '{}'", token.getUser().getUsername());
                    });
        }

        // Delete refresh cookie globally
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    // ============================================================
    // Helper: set secure HttpOnly refresh cookie
    // ============================================================
    private void setRefreshCookie(HttpServletResponse response, String refreshToken, Date expiry) {
        long maxAgeSeconds = Math.max(0L,
                (expiry.getTime() - System.currentTimeMillis()) / 1000);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(Duration.between(Instant.now(), expiry.toInstant()))
                .sameSite("None")
                .maxAge(maxAgeSeconds)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
