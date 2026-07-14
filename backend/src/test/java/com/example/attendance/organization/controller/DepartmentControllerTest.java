package com.example.attendance.organization.controller;

import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.auth.security.SecurityConfig;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.organization.dto.DepartmentResponse;
import com.example.attendance.organization.service.OrganizationService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("GET /api/v1/departments - 認証済みで部一覧を取得できる")
    @WithMockUser(roles = "EMPLOYEE")
    void findByOrganizationId_authenticated_returnsOk() throws Exception {
        when(organizationService.findDepartmentsByOrganizationId(1L))
                .thenReturn(List.of(new DepartmentResponse(1L, 1L, "第一営業部", "SALES1")));

        mockMvc.perform(get("/api/v1/departments").param("organizationId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("第一営業部"));
    }

    @Test
    @DisplayName("POST /api/v1/departments - ADMINで部を作成できる")
    @WithMockUser(roles = "ADMIN")
    void create_admin_returns201() throws Exception {
        when(organizationService.createDepartment(any()))
                .thenReturn(new DepartmentResponse(1L, 1L, "第一営業部", "SALES1"));

        mockMvc.perform(post("/api/v1/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationId\":1,\"name\":\"第一営業部\",\"code\":\"SALES1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/departments - EMPLOYEE権限では403")
    @WithMockUser(roles = "EMPLOYEE")
    void create_employee_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationId\":1,\"name\":\"部\",\"code\":\"D\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/departments/{id} - ADMINで削除できる")
    @WithMockUser(roles = "ADMIN")
    void delete_admin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/departments/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
