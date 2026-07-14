package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.TimeRecordRequest;
import com.example.attendance.attendance.dto.TimeRecordResponse;
import com.example.attendance.attendance.dto.TimeRecordStatusResponse;
import com.example.attendance.attendance.service.TimeRecordService;
import com.example.attendance.attendance.vo.ClockState;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimeRecordController.class)
class TimeRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TimeRecordService timeRecordService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private com.example.attendance.auth.security.JwtTokenProvider jwtTokenProvider;

    private void mockEmployee() {
        var employee = Employee.builder().id(1L).email("test@example.com").build();
        when(employeeRepository.findByEmail("test@example.com")).thenReturn(Optional.of(employee));
    }

    @Test
    @DisplayName("POST /api/v1/time-records → 201")
    @WithMockUser(username = "test@example.com")
    void record_returns201() throws Exception {
        mockEmployee();
        var response = new TimeRecordResponse(1L, TimeRecordType.CLOCK_IN,
                LocalDateTime.of(2026, 7, 14, 9, 0), false, null, null);
        when(timeRecordService.record(eq(1L), any(TimeRecordRequest.class))).thenReturn(response);

        var request = new TimeRecordRequest(TimeRecordType.CLOCK_IN, 35.6812, 139.7671);

        mockMvc.perform(post("/api/v1/time-records")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CLOCK_IN"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/time-records バリデーションエラー → 400")
    @WithMockUser(username = "test@example.com")
    void record_invalidRequest_returns400() throws Exception {
        mockEmployee();

        mockMvc.perform(post("/api/v1/time-records")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/time-records/status → リッチレスポンス")
    @WithMockUser(username = "test@example.com")
    void getStatus_returnsRichResponse() throws Exception {
        mockEmployee();
        var status = new TimeRecordStatusResponse(
                ClockState.WORKING,
                Set.of(TimeRecordType.BREAK_START, TimeRecordType.CLOCK_OUT),
                LocalTime.of(9, 0),
                195,
                0,
                List.of(new TimeRecordStatusResponse.RecordEntry(TimeRecordType.CLOCK_IN, LocalTime.of(9, 0)))
        );
        when(timeRecordService.getStatus(1L)).thenReturn(status);

        mockMvc.perform(get("/api/v1/time-records/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentState").value("WORKING"))
                .andExpect(jsonPath("$.clockInAt").value("09:00:00"))
                .andExpect(jsonPath("$.elapsedMinutes").value(195));
    }

    @Test
    @DisplayName("未認証 → 401 or 403")
    void unauthorized_returnsUnauthorizedOrForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/time-records/status"))
                .andExpect(status().isUnauthorized());
    }
}
