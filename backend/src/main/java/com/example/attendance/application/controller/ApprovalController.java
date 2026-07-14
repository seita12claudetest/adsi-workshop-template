package com.example.attendance.application.controller;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.dto.ApprovalRequest;
import com.example.attendance.application.dto.ApprovalResponse;
import com.example.attendance.application.service.ApprovalService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<ApplicationResponse> findPending(Authentication authentication) {
        Long approverId = Long.parseLong(authentication.getName());
        return approvalService.findPendingApprovals(approverId);
    }

    @PostMapping("/{applicationId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ApprovalResponse approve(
            @PathVariable Long applicationId,
            Authentication authentication,
            @RequestBody ApprovalRequest request) {
        Long approverId = Long.parseLong(authentication.getName());
        return approvalService.approve(applicationId, approverId, request.comment());
    }

    @PostMapping("/{applicationId}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ApprovalResponse reject(
            @PathVariable Long applicationId,
            Authentication authentication,
            @RequestBody ApprovalRequest request) {
        Long approverId = Long.parseLong(authentication.getName());
        return approvalService.reject(applicationId, approverId, request.comment());
    }
}
