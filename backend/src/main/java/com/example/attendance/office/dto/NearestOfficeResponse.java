package com.example.attendance.office.dto;

public record NearestOfficeResponse(
        OfficeResponse office,
        double distanceMeters,
        String distanceFormatted,
        boolean withinArea
) {}
