package com.example.attendance.leave.dto;

import java.math.BigDecimal;
import java.util.List;

public record LeaveBalanceSummaryResponse(
    Long employeeId,
    BigDecimal totalRemainingDays,
    List<LeaveBalanceResponse> balances
) {}
