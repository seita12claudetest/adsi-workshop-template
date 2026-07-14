package com.example.attendance.organization.controller;

import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.auth.security.SecurityConfig;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.organization.dto.OrganizationResponse;
import com.example.attendance.organization.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(OrganizationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("GET /api/v1/organizations - 認証済みで本部一覧を取得できる")
    @WithMockUser(roles = "EMPLOYEE")
    void findAll_authenticated_returnsOk() throws Exception {
        when(organizationService.findAllOrganizations())
                .thenReturn(List.of(new OrganizationResponse(1L, "営業本部", "SALES")));

        mockMvc.perform(get("/api/v1/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("営業本部"));
    }

    @Test
    @DisplayName("GET /api/v1/organizations - 未認証で401")
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/organizations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/organizations - ADMINで本部を作成できる")
    @WithMockUser(roles = "ADMIN")
    void create_admin_returns201() throws Exception {
        when(organizationService.createOrganization(any()))
                .thenReturn(new OrganizationResponse(1L, "営業本部", "SALES"));

        mockMvc.perform(post("/api/v1/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"営業本部\",\"code\":\"SALES\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("営業本部"));
    }

    @Test
    @DisplayName("POST /api/v1/organizations - EMPLOYEE権限では403")
    @WithMockUser(roles = "EMPLOYEE")
    void create_employee_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"営業本部\",\"code\":\"SALES\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/organizations - バリデーションエラーで400")
    @WithMockUser(roles = "ADMIN")
    void create_invalidRequest_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/organizations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"code\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/organizations/{id} - ADMINで更新できる")
    @WithMockUser(roles = "ADMIN")
    void update_admin_returnsOk() throws Exception {
        when(organizationService.updateOrganization(any(), any()))
                .thenReturn(new OrganizationResponse(1L, "新営業本部", "SALES"));

        mockMvc.perform(put("/api/v1/organizations/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"新営業本部\",\"code\":\"SALES\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("新営業本部"));
    }

    @Test
    @DisplayName("DELETE /api/v1/organizations/{id} - ADMINで削除できる")
    @WithMockUser(roles = "ADMIN")
    void delete_admin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/organizations/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
