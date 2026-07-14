package com.example.attendance.attendance.dto;

import com.example.attendance.common.enums.TimeRecordType;

import java.time.LocalDateTime;

public record TimeRecordResponse(
        Long id,
        TimeRecordType type,
        LocalDateTime recordedAt,
        boolean withinArea,
        String officeName,
        String warning
) {
}
