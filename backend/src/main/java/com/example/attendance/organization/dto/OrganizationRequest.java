package com.example.attendance.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationRequest(
        @NotBlank(message = "本部名は必須です")
        @Size(max = 100, message = "本部名は100文字以内で入力してください")
        String name,

        @NotBlank(message = "本部コードは必須です")
        @Size(max = 20, message = "本部コードは20文字以内で入力してください")
        String code
) {}
