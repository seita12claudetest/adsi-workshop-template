package com.example.attendance.office.dto;

import com.example.attendance.office.entity.Office;

import java.time.LocalDateTime;

public record OfficeResponse(
        Long id,
        String name,
        String address,
        double latitude,
        double longitude,
        int radiusMeters,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OfficeResponse from(Office office) {
        return new OfficeResponse(
                office.getId(),
                office.getName(),
                office.getAddress(),
                office.getLatitude(),
                office.getLongitude(),
                office.getRadiusMeters(),
                office.getCreatedAt(),
                office.getUpdatedAt()
        );
    }
}
