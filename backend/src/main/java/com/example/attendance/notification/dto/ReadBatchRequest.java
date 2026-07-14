package com.example.attendance.notification.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReadBatchRequest(
        @NotEmpty(message = "通知IDリストは必須です")
        List<Long> ids
) {
}
