package com.example.attendance.auth.service;

import com.example.attendance.auth.dto.LoginRequest;
import com.example.attendance.auth.dto.LoginResponse;
import com.example.attendance.auth.dto.TokenResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    TokenResponse refresh(String refreshToken);
}
