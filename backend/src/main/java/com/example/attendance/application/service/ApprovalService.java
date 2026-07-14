package com.example.attendance.application.service;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.dto.ApprovalResponse;

import java.util.List;

public interface ApprovalService {

    ApprovalResponse approve(Long applicationId, Long approverId, String comment);

    ApprovalResponse reject(Long applicationId, Long approverId, String comment);

    List<ApplicationResponse> findPendingApprovals(Long approverId);
}
