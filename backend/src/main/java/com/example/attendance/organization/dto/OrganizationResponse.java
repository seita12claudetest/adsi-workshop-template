package com.example.attendance.organization.dto;

import com.example.attendance.organization.entity.Organization;

public record OrganizationResponse(
        Long id,
        String name,
        String code
) {
    public static OrganizationResponse from(Organization entity) {
        return new OrganizationResponse(entity.getId(), entity.getName(), entity.getCode());
    }
}
