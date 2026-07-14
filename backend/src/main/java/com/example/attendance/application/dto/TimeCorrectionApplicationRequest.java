package com.example.attendance.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimeCorrectionApplicationRequest(
        @NotNull(message = "対象日は必須です")
        LocalDate date,

        LocalTime correctedClockIn,

        LocalTime correctedClockOut,

        String reason
) {}
