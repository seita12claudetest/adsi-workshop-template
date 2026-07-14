package com.example.attendance.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaveBalanceResponse(
    Long id,
    Integer fiscalYear,
    BigDecimal grantedDays,
    BigDecimal usedDays,
    BigDecimal remainingDays,
    LocalDate grantDate,
    LocalDate expiryDate
) {}
