package com.railway.user_service.dto;

import jakarta.validation.constraints.Size;

public record RegisterRequest(
                String username,
                String fullName,
                String email,
                String phoneNumber,
                @Size(min = 6, max = 100) String password) {

}
