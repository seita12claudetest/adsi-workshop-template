package com.example.attendance.application.controller;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.dto.ApprovalResponse;
import com.example.attendance.application.service.ApprovalService;
import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.auth.security.SecurityConfig;
import com.example.attendance.common.enums.ApplicationStatus;
import com.example.attendance.common.enums.ApplicationType;
import com.example.attendance.common.enums.ApprovalAction;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprovalController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApprovalService approvalService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("GET /api/v1/approvals/pending - MANAGER権限で未処理一覧を取得できる")
    @WithMockUser(username = "5", roles = "MANAGER")
    void findPending_manager_returnsOk() throws Exception {
        var response = new ApplicationResponse(
                1L, 10L, "田中太郎", ApplicationType.LEAVE, ApplicationStatus.PENDING,
                LocalDateTime.of(2026, 7, 14, 10, 0), "私用のため", null);
        when(approvalService.findPendingApprovals(5L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/approvals/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicantName").value("田中太郎"));
    }

    @Test
    @DisplayName("POST /api/v1/approvals/{id}/approve - 承認できる")
    @WithMockUser(username = "5", roles = "MANAGER")
    void approve_manager_returnsOk() throws Exception {
        var response = new ApprovalResponse(1L, ApprovalAction.APPROVED, LocalDateTime.of(2026, 7, 14, 11, 0));
        when(approvalService.approve(eq(1L), eq(5L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/approvals/1/approve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "comment": "承認します" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/v1/approvals/{id}/reject - 差戻できる")
    @WithMockUser(username = "5", roles = "MANAGER")
    void reject_manager_returnsOk() throws Exception {
        var response = new ApprovalResponse(1L, ApprovalAction.REJECTED, LocalDateTime.of(2026, 7, 14, 11, 0));
        when(approvalService.reject(eq(1L), eq(5L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/approvals/1/reject")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "comment": "日付を確認してください" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.action").value("REJECTED"));
    }

    @Test
    @DisplayName("GET /api/v1/approvals/pending - EMPLOYEE権限では403")
    @WithMockUser(username = "10", roles = "EMPLOYEE")
    void findPending_employee_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/approvals/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/approvals/{id}/approve - 未認証で401")
    void approve_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/approvals/1/approve")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "comment": "承認します" }
                            """))
                .andExpect(status().isUnauthorized());
    }
}
