package com.example.attendance.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotNull(message = "本部IDは必須です")
        Long organizationId,

        @NotBlank(message = "部名は必須です")
        @Size(max = 100, message = "部名は100文字以内で入力してください")
        String name,

        @NotBlank(message = "部コードは必須です")
        @Size(max = 20, message = "部コードは20文字以内で入力してください")
        String code
) {}
