package com.example.attendance.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        EmployeeInfo employee
) {
    public record EmployeeInfo(
            Long id,
            String name,
            String email,
            String role
    ) {}
}
