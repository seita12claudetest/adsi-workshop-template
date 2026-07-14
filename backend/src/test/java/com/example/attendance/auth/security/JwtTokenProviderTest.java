package com.example.attendance.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-for-testing-only-must-be-at-least-256-bits-long-here",
                3600000,
                86400000
        );
    }

    @Test
    @DisplayName("アクセストークンを生成し、社員IDを取得できる")
    void generateAccessToken_validInput_canExtractEmployeeId() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com", "EMPLOYEE");

        Long employeeId = jwtTokenProvider.getEmployeeIdFromToken(token);

        assertThat(employeeId).isEqualTo(1L);
    }

    @Test
    @DisplayName("有効なトークンの検証が成功する")
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com", "EMPLOYEE");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("無効なトークンの検証が失敗する")
    void validateToken_invalidToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
    }

    @Test
    @DisplayName("リフレッシュトークンを生成し検証できる")
    void generateRefreshToken_validInput_canValidate() {
        String token = jwtTokenProvider.generateRefreshToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getEmployeeIdFromToken(token)).isEqualTo(1L);
    }
}
