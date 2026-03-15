package com.railway.user_service.dto;

public record TokenValidationResponse(
        boolean valid,
        Long userId,
        String username,
        String role) {
}
