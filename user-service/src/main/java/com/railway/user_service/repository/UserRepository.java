package com.railway.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.railway.user_service.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByUsernameOrEmailOrPhoneNumber(String username, String email, String phoneNumber);

    boolean existsByUsernameOrEmailOrPhoneNumber(String username, String email, String phoneNumber);

}
