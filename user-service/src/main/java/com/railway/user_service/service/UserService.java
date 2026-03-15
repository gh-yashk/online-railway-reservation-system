package com.railway.user_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.railway.user_service.dto.UserResponse;
import com.railway.user_service.model.User;
import com.railway.user_service.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name());
    }

    public List<UserResponse> getUsers() {
        log.info("Fetching all users from database");
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getRole().name()))
                .toList();
    }

    public Optional<User> findByUsernameOrEmailOrPhoneNumber(String identifier) {
        log.info("Searching user by identifier: {}", identifier);
        return userRepository.findByUsernameOrEmailOrPhoneNumber(identifier, identifier, identifier);
    }

    public boolean existsByUsernameOrEmailOrPhoneNumber(String username, String email, String phoneNumber) {
        return userRepository.existsByUsernameOrEmailOrPhoneNumber(username, email, phoneNumber);
    }

    public void save(@NonNull User user) {
        log.info("Saving user: {}", user.getUsername());
        userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        log.info("Searching user by username: {}", username);
        return userRepository.findByUsername(username);
    }

}
