package com.example.attendance.application.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LeaveApplicationRequest(
        @NotNull(message = "休暇種別は必須です")
        String leaveType,

        @NotNull(message = "開始日は必須です")
        LocalDate startDate,

        @NotNull(message = "終了日は必須です")
        LocalDate endDate,

        BigDecimal hours,

        String reason
) {}
