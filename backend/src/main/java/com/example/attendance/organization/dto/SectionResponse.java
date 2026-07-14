package com.example.attendance.organization.dto;

import com.example.attendance.organization.entity.Section;

public record SectionResponse(
        Long id,
        Long departmentId,
        String name,
        String code,
        Long managerId
) {
    public static SectionResponse from(Section entity) {
        return new SectionResponse(
                entity.getId(),
                entity.getDepartmentId(),
                entity.getName(),
                entity.getCode(),
                entity.getManagerId()
        );
    }
}
