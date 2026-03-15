package com.railway.user_service.util;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private static JwtUtil instance;

    public JwtUtil() {
        JwtUtil.instance = this;
    }

    public static String generateToken(String username, String role) {
        Map<String, Object> claims = Map.of(
                "username", username,
                "role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + instance.expirationTime))
                .signWith(instance.getSigningKey())
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private static Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(instance.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String extractUsername(String token) throws JwtException {
        return extractAllClaims(token).getSubject();
    }

    public static boolean isTokenValid(String token) throws JwtException {
        return !isTokenExpired(token);
    }

    private static boolean isTokenExpired(String token) throws JwtException {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

}
