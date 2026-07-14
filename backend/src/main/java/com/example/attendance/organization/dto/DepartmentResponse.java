package com.example.attendance.organization.dto;

import com.example.attendance.organization.entity.Department;

public record DepartmentResponse(
        Long id,
        Long organizationId,
        String name,
        String code
) {
    public static DepartmentResponse from(Department entity) {
        return new DepartmentResponse(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getName(),
                entity.getCode()
        );
    }
}
