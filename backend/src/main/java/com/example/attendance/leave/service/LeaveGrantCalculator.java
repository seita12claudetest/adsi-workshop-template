package com.example.attendance.leave.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class LeaveGrantCalculator {

    private record GrantRule(int minMonths, BigDecimal days) {}

    private static final List<GrantRule> GRANT_TABLE = List.of(
        new GrantRule(78, BigDecimal.valueOf(20)),
        new GrantRule(66, BigDecimal.valueOf(18)),
        new GrantRule(54, BigDecimal.valueOf(16)),
        new GrantRule(42, BigDecimal.valueOf(14)),
        new GrantRule(30, BigDecimal.valueOf(12)),
        new GrantRule(18, BigDecimal.valueOf(11)),
        new GrantRule(6, BigDecimal.valueOf(10))
    );

    public BigDecimal calculate(int serviceMonths) {
        return GRANT_TABLE.stream()
            .filter(rule -> serviceMonths >= rule.minMonths())
            .map(GrantRule::days)
            .findFirst()
            .orElse(BigDecimal.ZERO);
    }

    public BigDecimal calculateForEmployee(LocalDate hireDate, LocalDate grantDate) {
        int months = (int) ChronoUnit.MONTHS.between(hireDate, grantDate);
        return calculate(months);
    }
}
