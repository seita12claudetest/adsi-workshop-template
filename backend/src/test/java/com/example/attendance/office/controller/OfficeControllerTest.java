package com.example.attendance.office.controller;

import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.office.dto.NearestOfficeResponse;
import com.example.attendance.office.dto.OfficeRequest;
import com.example.attendance.office.dto.OfficeResponse;
import com.example.attendance.office.service.OfficeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfficeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OfficeService officeService;

    private OfficeResponse sampleResponse() {
        return new OfficeResponse(1L, "本社", "東京都千代田区", 35.6812, 139.7671, 500,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    @DisplayName("認証済みユーザーが拠点一覧を取得できる")
    @WithMockUser(roles = "EMPLOYEE")
    void getOffices_authenticated_returns200() throws Exception {
        var pageable = PageRequest.of(0, 20);
        when(officeService.findAll(any())).thenReturn(new PageImpl<>(List.of(sampleResponse()), pageable, 1));

        mockMvc.perform(get("/api/v1/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("本社"));
    }

    @Test
    @DisplayName("未認証ユーザーは拠点一覧を取得できない")
    void getOffices_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/offices"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMINが拠点を登録できる")
    @WithMockUser(roles = "ADMIN")
    void createOffice_admin_returns201() throws Exception {
        var request = new OfficeRequest("新拠点", "東京都新宿区", 35.6896, 139.6922, 400);
        when(officeService.create(any(OfficeRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("本社"));
    }

    @Test
    @DisplayName("一般社員は拠点を登録できない（403）")
    @WithMockUser(roles = "EMPLOYEE")
    void createOffice_employee_returns403() throws Exception {
        var request = new OfficeRequest("新拠点", "東京都新宿区", 35.6896, 139.6922, 400);

        mockMvc.perform(post("/api/v1/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("バリデーションエラーで400が返る")
    @WithMockUser(roles = "ADMIN")
    void createOffice_invalidRequest_returns400() throws Exception {
        var request = new OfficeRequest("", "", null, null, null);

        mockMvc.perform(post("/api/v1/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("ADMINが拠点を更新できる")
    @WithMockUser(roles = "ADMIN")
    void updateOffice_admin_returns200() throws Exception {
        var request = new OfficeRequest("更新拠点", "更新住所", 36.0, 140.0, 600);
        when(officeService.update(eq(1L), any(OfficeRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/offices/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMINが拠点を削除できる")
    @WithMockUser(roles = "ADMIN")
    void deleteOffice_admin_returns204() throws Exception {
        doNothing().when(officeService).delete(1L);

        mockMvc.perform(delete("/api/v1/offices/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("存在しない拠点の削除で404が返る")
    @WithMockUser(roles = "ADMIN")
    void deleteOffice_nonExisting_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("拠点", 99L)).when(officeService).delete(99L);

        mockMvc.perform(delete("/api/v1/offices/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("認証済みユーザーが最寄り拠点を検索できる")
    @WithMockUser(roles = "EMPLOYEE")
    void getNearestOffice_authenticated_returns200() throws Exception {
        var response = new NearestOfficeResponse(sampleResponse(), 320.5, "321m", true);
        when(officeService.findNearest(35.6812, 139.7671)).thenReturn(response);

        mockMvc.perform(get("/api/v1/offices/nearest")
                        .param("latitude", "35.6812")
                        .param("longitude", "139.7671"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.withinArea").value(true))
                .andExpect(jsonPath("$.distanceFormatted").value("321m"));
    }

    @Test
    @DisplayName("拠点が未登録の場合204が返る")
    @WithMockUser(roles = "EMPLOYEE")
    void getNearestOffice_noOffices_returns204() throws Exception {
        when(officeService.findNearest(35.6812, 139.7671)).thenReturn(null);

        mockMvc.perform(get("/api/v1/offices/nearest")
                        .param("latitude", "35.6812")
                        .param("longitude", "139.7671"))
                .andExpect(status().isNoContent());
    }
}
