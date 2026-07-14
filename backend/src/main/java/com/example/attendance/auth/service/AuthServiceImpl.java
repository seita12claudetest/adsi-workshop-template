package com.example.attendance.auth.service;

import com.example.attendance.auth.dto.LoginRequest;
import com.example.attendance.auth.dto.LoginResponse;
import com.example.attendance.auth.dto.TokenResponse;
import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Employee employee = employeeRepository.findByEmail(request.email())
                .filter(Employee::isActive)
                .orElseThrow(() -> new BadCredentialsException("メールアドレスまたはパスワードが正しくありません"));

        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new BadCredentialsException("メールアドレスまたはパスワードが正しくありません");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                employee.getId(), employee.getEmail(), employee.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(employee.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getExpirationMs() / 1000,
                new LoginResponse.EmployeeInfo(
                        employee.getId(),
                        employee.getName(),
                        employee.getEmail(),
                        employee.getRole().name()
                )
        );
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("無効なリフレッシュトークンです");
        }

        Long employeeId = jwtTokenProvider.getEmployeeIdFromToken(refreshToken);
        Employee employee = employeeRepository.findById(employeeId)
                .filter(Employee::isActive)
                .orElseThrow(() -> new BadCredentialsException("無効なリフレッシュトークンです"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                employee.getId(), employee.getEmail(), employee.getRole().name());

        return new TokenResponse(newAccessToken, jwtTokenProvider.getExpirationMs() / 1000);
    }
}
