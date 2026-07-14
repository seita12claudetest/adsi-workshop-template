package com.example.attendance.leave.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LeaveGrantCalculatorTest {

    private final LeaveGrantCalculator calculator = new LeaveGrantCalculator();

    @ParameterizedTest
    @DisplayName("勤続月数に基づき法定付与日数を返す")
    @CsvSource({
        "0, 0",
        "5, 0",
        "6, 10",
        "17, 10",
        "18, 11",
        "29, 11",
        "30, 12",
        "41, 12",
        "42, 14",
        "53, 14",
        "54, 16",
        "65, 16",
        "66, 18",
        "77, 18",
        "78, 20",
        "120, 20"
    })
    void calculate_serviceMonths_returnsCorrectDays(int serviceMonths, int expectedDays) {
        BigDecimal result = calculator.calculate(serviceMonths);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(expectedDays));
    }

    @ParameterizedTest
    @DisplayName("入社日と付与日から正しい付与日数を算出する")
    @CsvSource({
        "2025-10-01, 2026-04-01, 10",
        "2024-10-01, 2026-04-01, 11",
        "2020-10-01, 2026-04-01, 18",
        "2019-04-01, 2026-04-01, 20"
    })
    void calculateForEmployee_hireAndGrantDate_returnsCorrectDays(
            LocalDate hireDate, LocalDate grantDate, int expectedDays) {
        BigDecimal result = calculator.calculateForEmployee(hireDate, grantDate);
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(expectedDays));
    }

    @ParameterizedTest
    @DisplayName("入社6ヶ月未満は付与対象外")
    @CsvSource({
        "2026-04-01, 2026-04-01",
        "2025-11-01, 2026-04-01"
    })
    void calculateForEmployee_lessThanSixMonths_returnsZero(
            LocalDate hireDate, LocalDate grantDate) {
        BigDecimal result = calculator.calculateForEmployee(hireDate, grantDate);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
