package com.example.attendance.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank(message = "社員コードは必須です")
        @Size(max = 20, message = "社員コードは20文字以内で入力してください")
        String employeeCode,

        @NotBlank(message = "氏名は必須です")
        @Size(max = 100, message = "氏名は100文字以内で入力してください")
        String name,

        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "メールアドレスの形式が正しくありません")
        String email,

        @Size(min = 8, message = "パスワードは8文字以上で入力してください")
        String password,

        @NotBlank(message = "ロールは必須です")
        String role,

        @NotNull(message = "所属課IDは必須です")
        Long sectionId,

        @NotNull(message = "入社日は必須です")
        LocalDate hireDate
) {}
