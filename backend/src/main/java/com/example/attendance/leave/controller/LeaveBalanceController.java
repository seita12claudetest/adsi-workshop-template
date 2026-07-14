package com.example.attendance.leave.controller;

import com.example.attendance.leave.dto.GrantResultResponse;
import com.example.attendance.leave.dto.LeaveBalanceSummaryResponse;
import com.example.attendance.leave.service.LeaveBalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/leave-balances")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    public LeaveBalanceController(LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    @GetMapping
    public ResponseEntity<LeaveBalanceSummaryResponse> getMyBalance(Authentication authentication) {
        Long employeeId = Long.valueOf(authentication.getName());
        return ResponseEntity.ok(leaveBalanceService.getBalance(employeeId));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveBalanceSummaryResponse> getEmployeeBalance(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveBalanceService.getBalance(employeeId));
    }

    @PostMapping("/grant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GrantResultResponse> grantAnnualLeave(@RequestParam int fiscalYear) {
        return ResponseEntity.ok(leaveBalanceService.grantAnnualLeave(fiscalYear));
    }

    @PostMapping("/expire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> expireBalances() {
        int count = leaveBalanceService.expireOutdatedBalances();
        return ResponseEntity.ok(Map.of("expiredCount", count));
    }
}
