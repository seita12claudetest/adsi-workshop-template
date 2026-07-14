package com.example.attendance.application.controller;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.service.ApplicationService;
import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.auth.security.SecurityConfig;
import com.example.attendance.common.enums.ApplicationStatus;
import com.example.attendance.common.enums.ApplicationType;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    private final ApplicationResponse sampleResponse = new ApplicationResponse(
            1L, 1L, "田中太郎", ApplicationType.LEAVE, ApplicationStatus.PENDING,
            LocalDateTime.of(2026, 7, 14, 10, 0), "私用のため", null);

    @Test
    @DisplayName("POST /api/v1/applications/leave - 休暇申請を作成できる")
    @WithMockUser(username = "1", roles = "EMPLOYEE")
    void createLeave_authenticated_returns201() throws Exception {
        when(applicationService.createLeaveApplication(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/applications/leave")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "leaveType": "ANNUAL",
                              "startDate": "2026-07-20",
                              "endDate": "2026-07-20",
                              "reason": "私用のため"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("LEAVE"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/applications/overtime - 残業申請を作成できる")
    @WithMockUser(username = "1", roles = "EMPLOYEE")
    void createOvertime_authenticated_returns201() throws Exception {
        var response = new ApplicationResponse(
                2L, 1L, "田中太郎", ApplicationType.OVERTIME, ApplicationStatus.PENDING,
                LocalDateTime.of(2026, 7, 14, 10, 0), "納期対応", null);
        when(applicationService.createOvertimeApplication(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/applications/overtime")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "date": "2026-07-14",
                              "expectedMinutes": 60,
                              "overtimeType": "PRE",
                              "reason": "納期対応"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("OVERTIME"));
    }

    @Test
    @DisplayName("POST /api/v1/applications/time-correction - 打刻修正申請を作成できる")
    @WithMockUser(username = "1", roles = "EMPLOYEE")
    void createTimeCorrection_authenticated_returns201() throws Exception {
        var response = new ApplicationResponse(
                3L, 1L, "田中太郎", ApplicationType.TIME_CORRECTION, ApplicationStatus.PENDING,
                LocalDateTime.of(2026, 7, 14, 10, 0), "退勤打刻忘れ", null);
        when(applicationService.createTimeCorrectionApplication(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/applications/time-correction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "date": "2026-07-13",
                              "correctedClockIn": "09:15",
                              "correctedClockOut": "18:30",
                              "reason": "退勤打刻忘れ"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TIME_CORRECTION"));
    }

    @Test
    @DisplayName("GET /api/v1/applications - 自分の申請一覧を取得できる")
    @WithMockUser(username = "1", roles = "EMPLOYEE")
    void findMyApplications_authenticated_returnsOk() throws Exception {
        when(applicationService.findByApplicantId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("LEAVE"));
    }

    @Test
    @DisplayName("GET /api/v1/applications/{id} - 申請詳細を取得できる")
    @WithMockUser(username = "1", roles = "EMPLOYEE")
    void findById_authenticated_returnsOk() throws Exception {
        when(applicationService.findById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicantName").value("田中太郎"));
    }

    @Test
    @DisplayName("DELETE /api/v1/applications/{id} - 申請を取消できる")
    @WithMockUser(username = "1", roles = "EMPLOYEE")
    void cancel_authenticated_returns204() throws Exception {
        doNothing().when(applicationService).cancel(1L, 1L);

        mockMvc.perform(delete("/api/v1/applications/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/applications/leave - 未認証で401")
    void createLeave_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/applications/leave")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "leaveType": "ANNUAL",
                              "startDate": "2026-07-20",
                              "endDate": "2026-07-20"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }
}
