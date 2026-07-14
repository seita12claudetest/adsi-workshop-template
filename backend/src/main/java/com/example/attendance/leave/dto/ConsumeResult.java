package com.example.attendance.leave.dto;

import java.math.BigDecimal;
import java.util.List;

public record ConsumeResult(
    BigDecimal totalConsumed,
    List<ConsumeDetail> details
) {
    public record ConsumeDetail(
        Integer fiscalYear,
        BigDecimal consumed
    ) {}
}
