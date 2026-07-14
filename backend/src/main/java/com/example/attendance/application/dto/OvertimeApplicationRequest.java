package com.example.attendance.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record OvertimeApplicationRequest(
        @NotNull(message = "残業予定日は必須です")
        LocalDate date,

        @NotNull(message = "予定残業時間は必須です")
        @Positive(message = "予定残業時間は正の値を指定してください")
        Integer expectedMinutes,

        @NotNull(message = "残業種別は必須です")
        String overtimeType,

        String reason
) {}
