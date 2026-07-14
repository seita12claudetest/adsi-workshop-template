package com.example.attendance.leave.service;

import com.example.attendance.leave.dto.ConsumeResult;
import com.example.attendance.leave.dto.GrantResultResponse;
import com.example.attendance.leave.dto.LeaveBalanceSummaryResponse;

import java.math.BigDecimal;

public interface LeaveBalanceService {

    LeaveBalanceSummaryResponse getBalance(Long employeeId);

    boolean hasEnoughBalance(Long employeeId, BigDecimal days);

    ConsumeResult consume(Long employeeId, BigDecimal days);

    GrantResultResponse grantAnnualLeave(int fiscalYear);

    int expireOutdatedBalances();
}
