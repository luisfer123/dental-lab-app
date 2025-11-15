package com.dentallab.security.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dentallab.security.jwt.JwtUtil;
import com.dentallab.security.service.CustomUserDetailsService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter
 * - Extracts and validates access JWT from Authorization header
 * - Ensures token type is "access" (not "refresh")
 * - Loads UserDetails and sets SecurityContext
 * - Skips known unauthenticated endpoints and preflight requests
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Skip filtering for:
     *  - CORS preflight (OPTIONS)
     *  - Auth endpoints (/auth/**)
     *  - Error page (/error)
     *  - Health endpoint (/actuator/health) — optional, keep if you expose Actuator
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();

        if (HttpMethod.OPTIONS.matches(request.getMethod())) return true;
        if (PATH_MATCHER.match("/auth/**", path)) return true;
        if (PATH_MATCHER.match("/error", path)) return true;
        if (PATH_MATCHER.match("/actuator/health", path)) return true; // optional
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                String token = authHeader.substring(BEARER_PREFIX.length()).trim();

                // 🔹 validateToken lanzará excepción si el JWT está expirado o inválido
                jwtUtil.validateToken(token);

                // 🔹 Aceptamos solo tokens de tipo "access"
                String typ = jwtUtil.getType(token);
                if (!"access".equals(typ)) {
                    log.debug("⚠️ Invalid token type (expected 'access')");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"invalid_token_type\"}");
                    return;
                }

                // 🔹 Evita sobreescribir autenticación existente
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String username = jwtUtil.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            // ✅ Si todo fue bien, continuar con la cadena
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.debug("⏰ Expired JWT in request {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"token_expired\"}");
            response.getWriter().flush();
            return;

        } catch (JwtException | IllegalArgumentException e) {
            log.debug("🚫 Invalid JWT in request {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"invalid_token\"}");
            response.getWriter().flush();
            return;
        }
    }

}
