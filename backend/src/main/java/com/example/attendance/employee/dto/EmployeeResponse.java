package com.example.attendance.employee.dto;

import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.entity.Employee;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String name,
        String email,
        Role role,
        Long sectionId,
        LocalDate hireDate,
        boolean active
) {
    public static EmployeeResponse from(Employee entity) {
        return new EmployeeResponse(
                entity.getId(),
                entity.getEmployeeCode(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getSectionId(),
                entity.getHireDate(),
                entity.isActive()
        );
    }
}
