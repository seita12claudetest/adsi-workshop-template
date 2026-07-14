package com.example.attendance.office.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OfficeRequest(
        @NotBlank(message = "拠点名は必須です")
        @Size(max = 100, message = "拠点名は100文字以内で入力してください")
        String name,

        @NotBlank(message = "住所は必須です")
        @Size(max = 255, message = "住所は255文字以内で入力してください")
        String address,

        @NotNull(message = "緯度は必須です")
        @DecimalMin(value = "-90.0", message = "緯度は-90以上を指定してください")
        @DecimalMax(value = "90.0", message = "緯度は90以下を指定してください")
        Double latitude,

        @NotNull(message = "経度は必須です")
        @DecimalMin(value = "-180.0", message = "経度は-180以上を指定してください")
        @DecimalMax(value = "180.0", message = "経度は180以下を指定してください")
        Double longitude,

        @NotNull(message = "許可半径は必須です")
        @Min(value = 1, message = "許可半径は1m以上を指定してください")
        Integer radiusMeters
) {}
