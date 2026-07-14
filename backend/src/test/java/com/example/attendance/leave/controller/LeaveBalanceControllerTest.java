package com.example.attendance.leave.controller;

import com.example.attendance.leave.dto.GrantResultResponse;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveBalanceSummaryResponse;
import com.example.attendance.leave.service.LeaveBalanceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeaveBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaveBalanceService leaveBalanceService;

    private LeaveBalanceSummaryResponse sampleSummary(Long employeeId) {
        var balance = new LeaveBalanceResponse(
            1L, 2026, BigDecimal.valueOf(20), BigDecimal.valueOf(5),
            BigDecimal.valueOf(15), LocalDate.of(2026, 4, 1), LocalDate.of(2028, 3, 31)
        );
        return new LeaveBalanceSummaryResponse(employeeId, BigDecimal.valueOf(15), List.of(balance));
    }

    @Test
    @DisplayName("認証済みユーザーが自分の有給残高を取得できる")
    @WithMockUser(username = "1", roles = "EMPLOYEE")
    void getMyBalance_authenticated_returns200() throws Exception {
        when(leaveBalanceService.getBalance(1L)).thenReturn(sampleSummary(1L));

        mockMvc.perform(get("/api/v1/leave-balances"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRemainingDays").value(15))
            .andExpect(jsonPath("$.balances[0].fiscalYear").value(2026));
    }

    @Test
    @DisplayName("管理者が指定社員の有給残高を取得できる")
    @WithMockUser(username = "99", roles = "MANAGER")
    void getEmployeeBalance_manager_returns200() throws Exception {
        when(leaveBalanceService.getBalance(1L)).thenReturn(sampleSummary(1L));

        mockMvc.perform(get("/api/v1/leave-balances/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.totalRemainingDays").value(15));
    }

    @Test
    @DisplayName("未認証ユーザーはアクセスできない")
    void getMyBalance_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/leave-balances"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMINが一斉付与を実行できる")
    @WithMockUser(username = "99", roles = "ADMIN")
    void grantAnnualLeave_admin_returns200() throws Exception {
        when(leaveBalanceService.grantAnnualLeave(2026))
            .thenReturn(new GrantResultResponse(10, 2, 0));

        mockMvc.perform(post("/api/v1/leave-balances/grant")
                .param("fiscalYear", "2026"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.granted").value(10))
            .andExpect(jsonPath("$.skipped").value(2));
    }

    @Test
    @DisplayName("一般社員は一斉付与を実行できない（403）")
    @WithMockUser(username = "1", roles = "EMPLOYEE")
    void grantAnnualLeave_employee_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/leave-balances/grant")
                .param("fiscalYear", "2026"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMINが失効処理を実行できる")
    @WithMockUser(username = "99", roles = "ADMIN")
    void expireBalances_admin_returns200() throws Exception {
        when(leaveBalanceService.expireOutdatedBalances()).thenReturn(3);

        mockMvc.perform(post("/api/v1/leave-balances/expire"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.expiredCount").value(3));
    }
}
