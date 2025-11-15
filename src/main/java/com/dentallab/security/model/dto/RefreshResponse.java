package com.dentallab.security.model.dto;

/**
 * DTO returned when refreshing access token.
 */
public record RefreshResponse(String accessToken) {}