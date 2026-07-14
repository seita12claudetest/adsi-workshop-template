package com.example.attendance.attendance.dto;

import com.example.attendance.common.enums.MonthlyStatus;

import java.math.BigDecimal;

public record MonthlyAttendanceResponse(
        String yearMonth,
        int totalWorkingMinutes,
        int totalOvertimeMinutes,
        int workingDays,
        BigDecimal paidLeaveDays,
        MonthlyStatus status
) {
}
