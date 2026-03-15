package com.railway.train_service.dto;

public record ValidationResponse(
        boolean valid,
        Long userId,
        String username,
        String role) {

}
