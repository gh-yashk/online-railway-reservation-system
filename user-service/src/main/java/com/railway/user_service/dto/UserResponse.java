package com.railway.user_service.dto;

public record UserResponse(
        String username,
        String email,
        String phoneNumber,
        String Role) {
}
