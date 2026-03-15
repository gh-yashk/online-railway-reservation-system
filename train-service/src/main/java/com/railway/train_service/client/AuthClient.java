package com.railway.train_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.railway.train_service.dto.ValidationResponse;

@FeignClient(name = "user-service", path = "/api/auth")
public interface AuthClient {

    @GetMapping("/validate")
    ValidationResponse validateToken(@RequestHeader("Authorization") String token);

}
