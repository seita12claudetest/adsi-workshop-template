package com.example.attendance.application.dto;

import com.example.attendance.common.enums.ApprovalAction;

import java.time.LocalDateTime;

public record ApprovalResponse(
        Long applicationId,
        ApprovalAction action,
        LocalDateTime decidedAt
) {}
