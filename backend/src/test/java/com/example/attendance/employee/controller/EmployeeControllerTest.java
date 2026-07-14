package com.example.attendance.employee.controller;

import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.auth.security.SecurityConfig;
import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.dto.EmployeeResponse;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.employee.service.EmployeeService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    private final EmployeeResponse sampleResponse = new EmployeeResponse(
            1L, "EMP001", "田中太郎", "tanaka@example.com",
            Role.EMPLOYEE, 1L, LocalDate.of(2020, 4, 1), true);

    @Test
    @DisplayName("GET /api/v1/employees - ADMIN権限で社員一覧を取得できる")
    @WithMockUser(roles = "ADMIN")
    void findAll_admin_returnsOk() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("田中太郎"));
    }

    @Test
    @DisplayName("GET /api/v1/employees - EMPLOYEE権限では403")
    @WithMockUser(roles = "EMPLOYEE")
    void findAll_employee_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/employees/me - 全ロールで自分の情報を取得できる")
    @WithMockUser(username = "tanaka@example.com", roles = "EMPLOYEE")
    void findMe_authenticated_returnsOk() throws Exception {
        when(employeeService.findByEmail("tanaka@example.com")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/employees/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("田中太郎"));
    }

    @Test
    @DisplayName("POST /api/v1/employees - ADMINで社員を登録できる")
    @WithMockUser(roles = "ADMIN")
    void create_admin_returns201() throws Exception {
        when(employeeService.create(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "employeeCode": "EMP001",
                              "name": "田中太郎",
                              "email": "tanaka@example.com",
                              "password": "password123",
                              "role": "EMPLOYEE",
                              "sectionId": 1,
                              "hireDate": "2020-04-01"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("田中太郎"));
    }

    @Test
    @DisplayName("POST /api/v1/employees - EMPLOYEE権限では403")
    @WithMockUser(roles = "EMPLOYEE")
    void create_employee_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "employeeCode": "EMP001",
                              "name": "田中太郎",
                              "email": "tanaka@example.com",
                              "password": "password123",
                              "role": "EMPLOYEE",
                              "sectionId": 1,
                              "hireDate": "2020-04-01"
                            }
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} - ADMINで無効化できる")
    @WithMockUser(roles = "ADMIN")
    void deactivate_admin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/employees/me - 未認証で401")
    void findMe_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/employees/me"))
                .andExpect(status().isUnauthorized());
    }
}
