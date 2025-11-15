package com.dentallab.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Global CORS configuration.
 *
 * Allows Angular frontend (localhost:4200) to communicate with the Spring Boot backend
 * using HttpOnly cookies for refresh tokens and Authorization headers for access tokens.
 *
 * In production, adjust the allowed origins list to match your deployed frontend URLs.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Required for sending HttpOnly cookies (refresh token)
        config.setAllowCredentials(true);

        // Allow only trusted frontends
        config.setAllowedOrigins(List.of(
                "http://localhost:4200" // dev Angular app
                // Add production domains here:
                // "https://your-production-frontend.com"
        ));

        // Allowed headers the frontend may send
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept"
        ));

        // Methods permitted
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Headers the backend can expose to the frontend
        config.setExposedHeaders(List.of(
                "Authorization"
        ));

        // Optional: set max age for preflight (CORS cache)
        config.setMaxAge(3600L);

        // Apply configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
