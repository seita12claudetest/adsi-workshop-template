package com.example.attendance.common.dto;

import java.util.List;

public record ErrorResponse(
        String error,
        String message,
        List<FieldError> details
) {
    public record FieldError(
            String field,
            String message
    ) {}

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, List.of());
    }

    public static ErrorResponse of(String error, String message, List<FieldError> details) {
        return new ErrorResponse(error, message, details);
    }
}
