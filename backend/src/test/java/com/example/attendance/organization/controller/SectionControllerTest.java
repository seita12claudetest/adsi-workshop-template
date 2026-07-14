package com.example.attendance.organization.controller;

import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.auth.security.SecurityConfig;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.organization.dto.SectionResponse;
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

@WebMvcTest(SectionController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("GET /api/v1/sections - 認証済みで課一覧を取得できる")
    @WithMockUser(roles = "EMPLOYEE")
    void findByDepartmentId_authenticated_returnsOk() throws Exception {
        when(organizationService.findSectionsByDepartmentId(1L))
                .thenReturn(List.of(new SectionResponse(1L, 1L, "営業一課", "S1-1", null)));

        mockMvc.perform(get("/api/v1/sections").param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("営業一課"));
    }

    @Test
    @DisplayName("POST /api/v1/sections - ADMINで課を作成できる")
    @WithMockUser(roles = "ADMIN")
    void create_admin_returns201() throws Exception {
        when(organizationService.createSection(any()))
                .thenReturn(new SectionResponse(1L, 1L, "営業一課", "S1-1", null));

        mockMvc.perform(post("/api/v1/sections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentId\":1,\"name\":\"営業一課\",\"code\":\"S1-1\",\"managerId\":null}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/sections - EMPLOYEE権限では403")
    @WithMockUser(roles = "EMPLOYEE")
    void create_employee_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/sections")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"departmentId\":1,\"name\":\"課\",\"code\":\"S\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/sections/{id} - ADMINで削除できる")
    @WithMockUser(roles = "ADMIN")
    void delete_admin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/sections/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
