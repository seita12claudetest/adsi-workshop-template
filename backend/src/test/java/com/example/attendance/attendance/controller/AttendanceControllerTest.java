package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.DailyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.service.AttendanceService;
import com.example.attendance.common.enums.DailyStatus;
import com.example.attendance.common.enums.MonthlyStatus;
import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private com.example.attendance.auth.security.JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /api/v1/attendances/daily?date=2026-07-14 → 200")
    @WithMockUser(username = "test@example.com")
    void getDaily_byDate_returns200() throws Exception {
        var employee = Employee.builder().id(1L).email("test@example.com").role(Role.EMPLOYEE).build();
        when(employeeRepository.findByEmail("test@example.com")).thenReturn(Optional.of(employee));

        var response = new DailyAttendanceResponse(
                LocalDate.of(2026, 7, 14), LocalTime.of(9, 15), LocalTime.of(17, 30),
                LocalTime.of(12, 0), LocalTime.of(13, 0),
                435, 0, 60, DailyStatus.NORMAL);
        when(attendanceService.getDailyByDate(eq(1L), eq(LocalDate.of(2026, 7, 14)))).thenReturn(response);

        mockMvc.perform(get("/api/v1/attendances/daily").param("date", "2026-07-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workingMinutes").value(435))
                .andExpect(jsonPath("$.status").value("NORMAL"));
    }

    @Test
    @DisplayName("GET /api/v1/attendances/daily?yearMonth=2026-07 → リスト")
    @WithMockUser(username = "test@example.com")
    void getDaily_byMonth_returnsList() throws Exception {
        var employee = Employee.builder().id(1L).email("test@example.com").role(Role.EMPLOYEE).build();
        when(employeeRepository.findByEmail("test@example.com")).thenReturn(Optional.of(employee));

        var responses = List.of(
                new DailyAttendanceResponse(LocalDate.of(2026, 7, 1), null, null, null, null, 435, 0, 60, DailyStatus.NORMAL),
                new DailyAttendanceResponse(LocalDate.of(2026, 7, 2), null, null, null, null, 480, 45, 60, DailyStatus.NORMAL)
        );
        when(attendanceService.getDailyByMonth(1L, "2026-07")).thenReturn(responses);

        mockMvc.perform(get("/api/v1/attendances/daily").param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workingMinutes").value(435))
                .andExpect(jsonPath("$[1].overtimeMinutes").value(45));
    }

    @Test
    @DisplayName("GET /api/v1/attendances/monthly?yearMonth=2026-07 → 200")
    @WithMockUser(username = "test@example.com")
    void getMonthly_returns200() throws Exception {
        var employee = Employee.builder().id(1L).email("test@example.com").role(Role.EMPLOYEE).build();
        when(employeeRepository.findByEmail("test@example.com")).thenReturn(Optional.of(employee));

        var response = new MonthlyAttendanceResponse("2026-07", 8700, 120, 20, BigDecimal.ONE, MonthlyStatus.OPEN);
        when(attendanceService.getMonthly(1L, "2026-07")).thenReturn(response);

        mockMvc.perform(get("/api/v1/attendances/monthly").param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorkingMinutes").value(8700))
                .andExpect(jsonPath("$.workingDays").value(20));
    }

    @Test
    @DisplayName("部下一覧: EMPLOYEE → 403")
    @WithMockUser(username = "test@example.com")
    void subordinates_asEmployee_returns403() throws Exception {
        var employee = Employee.builder().id(1L).email("test@example.com").role(Role.EMPLOYEE).build();
        when(employeeRepository.findByEmail("test@example.com")).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/v1/attendances/monthly/subordinates").param("yearMonth", "2026-07"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未認証 → 401")
    void unauthorized_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/attendances/daily").param("date", "2026-07-14"))
                .andExpect(status().isUnauthorized());
    }
}
