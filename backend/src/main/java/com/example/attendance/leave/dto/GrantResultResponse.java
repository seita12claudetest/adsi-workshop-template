package com.example.attendance.leave.dto;

public record GrantResultResponse(
    int granted,
    int skipped,
    int alreadyGranted
) {}
