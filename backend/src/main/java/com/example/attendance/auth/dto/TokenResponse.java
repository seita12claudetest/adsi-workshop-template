package com.example.attendance.auth.dto;

public record TokenResponse(
        String accessToken,
        long expiresIn
) {}
