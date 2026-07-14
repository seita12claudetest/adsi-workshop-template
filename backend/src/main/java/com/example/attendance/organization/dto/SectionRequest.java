package com.example.attendance.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SectionRequest(
        @NotNull(message = "部IDは必須です")
        Long departmentId,

        @NotBlank(message = "課名は必須です")
        @Size(max = 100, message = "課名は100文字以内で入力してください")
        String name,

        @NotBlank(message = "課コードは必須です")
        @Size(max = 20, message = "課コードは20文字以内で入力してください")
        String code,

        Long managerId
) {}
