package com.railway.user_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.railway.user_service.dto.UserResponse;
import com.railway.user_service.model.User;
import com.railway.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management API", description = "Endpoints for managing user profiles and information")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Retrieve the profile of the currently authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        log.info("Fetching details for current user: {}", user.getUsername());
        return ResponseEntity.ok(userService.getCurrentUser(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieve a list of all users (requires ADMIN role)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Fetching all users - Admin access");
        return ResponseEntity.ok(userService.getUsers());
    }

}
