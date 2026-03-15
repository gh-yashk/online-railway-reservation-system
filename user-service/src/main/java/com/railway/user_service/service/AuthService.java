package com.railway.user_service.service;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.railway.user_service.dto.AuthResponse;
import com.railway.user_service.dto.LoginRequest;
import com.railway.user_service.dto.RegisterRequest;
import com.railway.user_service.dto.TokenValidationResponse;
import com.railway.user_service.exception.DuplicateResourceException;
import com.railway.user_service.model.Role;
import com.railway.user_service.model.User;
import com.railway.user_service.util.JwtUtil;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.username());
        if (userService.existsByUsernameOrEmailOrPhoneNumber(request.username(), request.email(),
                request.phoneNumber())) {
            log.warn("Registration failed - User already exists: {}", request.username());
            throw new DuplicateResourceException("Username, email, or phone number is already taken");
        }

        User user = User.builder()
                .username(request.username())
                .fullName(request.fullName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_PASSENGER)
                .build();

        userService.save(user);

        String token = JwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(token, userService.getCurrentUser(user));
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for identifier: {}", request.identifier());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.identifier(),
                        request.password()));

        User user = (User) authentication.getPrincipal();
        String token = JwtUtil.generateToken(user.getUsername(), user.getRole().name());

        log.info("Login successful for user: {}", user.getUsername());
        return new AuthResponse(token, userService.getCurrentUser(user));
    }

    public TokenValidationResponse validateToken(String token) {
        log.info("Validating JWT token");
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            } else {
                throw new BadCredentialsException("Invalid Authorization header format. Expected 'Bearer <token>'");
            }

            if (!JwtUtil.isTokenValid(token)) {
                throw new BadCredentialsException("Invalid JWT token");
            }

            String username = JwtUtil.extractUsername(token);
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty()) {
                throw new BadCredentialsException("User not found for token");
            }

            User user = userOptional.get();

            return new TokenValidationResponse(
                    true,
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid or expired JWT token");
        }
    }

}
