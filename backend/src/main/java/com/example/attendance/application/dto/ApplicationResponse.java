package com.example.attendance.application.dto;

import com.example.attendance.common.enums.ApplicationStatus;
import com.example.attendance.common.enums.ApplicationType;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long applicantId,
        String applicantName,
        ApplicationType type,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        String reason,
        Object detail
) {}
