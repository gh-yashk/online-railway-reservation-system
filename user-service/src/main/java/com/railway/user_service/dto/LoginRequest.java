package com.railway.user_service.dto;

public record LoginRequest(
        String identifier, // can be username, email, or phone number
        String password) {

}
