package com.example.attendance.attendance.dto;

import com.example.attendance.common.enums.DailyStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record DailyAttendanceResponse(
        LocalDate date,
        LocalTime clockIn,
        LocalTime clockOut,
        LocalTime breakStart,
        LocalTime breakEnd,
        Integer workingMinutes,
        Integer overtimeMinutes,
        Integer breakMinutes,
        DailyStatus status
) {
}
