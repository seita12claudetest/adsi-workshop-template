package com.example.attendance.attendance.dto;

import com.example.attendance.common.enums.TimeRecordType;
import jakarta.validation.constraints.NotNull;

public record TimeRecordRequest(
        @NotNull TimeRecordType type,
        Double latitude,
        Double longitude
) {
}
