package com.dentallab.security.model.dto;

/**
 * DTO received on login request.
 */
public record LoginRequest(String username, String password) {}