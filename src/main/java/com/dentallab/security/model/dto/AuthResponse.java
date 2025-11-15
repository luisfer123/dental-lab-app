package com.dentallab.security.model.dto;

import java.util.List;

/**
 * DTO returned on login with access token and roles.
 */
public record AuthResponse(String accessToken, List<String> roles) {}