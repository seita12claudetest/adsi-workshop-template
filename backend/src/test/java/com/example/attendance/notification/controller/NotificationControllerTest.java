package com.example.attendance.notification.controller;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.UnreadCountResponse;
import com.example.attendance.notification.service.NotificationService;
import com.example.attendance.notification.sse.NotificationSseEmitterManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationSseEmitterManager sseEmitterManager;

    @Test
    @DisplayName("GET /api/v1/notifications: 通知一覧が返される")
    void getNotifications_returnsPagedList() throws Exception {
        setupAuth(1L);
        NotificationResponse response = createResponse(1L, NotificationType.APPROVAL_REQUEST);
        when(notificationService.getNotifications(eq(1L), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("APPROVAL_REQUEST"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/notifications?type=CLOCK_REMINDER: タイプフィルタが動作する")
    void getNotifications_withTypeFilter_filtersCorrectly() throws Exception {
        setupAuth(1L);
        NotificationResponse response = createResponse(1L, NotificationType.CLOCK_REMINDER);
        when(notificationService.getNotifications(eq(1L), eq(NotificationType.CLOCK_REMINDER), any()))
                .thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/notifications").param("type", "CLOCK_REMINDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("CLOCK_REMINDER"));
    }

    @Test
    @DisplayName("GET /api/v1/notifications/unread-count: 未読数が返される")
    void getUnreadCount_returnsCount() throws Exception {
        setupAuth(1L);
        when(notificationService.getUnreadCount(1L)).thenReturn(new UnreadCountResponse(5));

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(5));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/{id}/read: 個別既読")
    void markAsRead_returnsUpdatedNotification() throws Exception {
        setupAuth(1L);
        NotificationResponse response = new NotificationResponse(
                10L, 1L, NotificationType.APPROVAL_REQUEST, "テスト", "メッセージ",
                true, LocalDateTime.now(), null);
        when(notificationService.markAsRead(10L, 1L)).thenReturn(response);

        mockMvc.perform(put("/api/v1/notifications/10/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/read-all: 全件既読")
    void markAllAsRead_returns200() throws Exception {
        setupAuth(1L);

        mockMvc.perform(put("/api/v1/notifications/read-all"))
                .andExpect(status().isOk());

        verify(notificationService).markAllAsRead(1L);
    }

    @Test
    @DisplayName("PUT /api/v1/notifications/read-batch: 一括既読")
    void markBatchAsRead_returns200() throws Exception {
        setupAuth(1L);
        String body = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("ids", List.of(1L, 2L, 3L));
        }});

        mockMvc.perform(put("/api/v1/notifications/read-batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(notificationService).markBatchAsRead(List.of(1L, 2L, 3L), 1L);
    }

    @Test
    @DisplayName("未認証アクセス: 401が返される")
    void unauthenticated_returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    private void setupAuth(Long employeeId) {
        Employee employee = Employee.builder()
                .id(employeeId)
                .employeeCode("EMP001")
                .name("テスト太郎")
                .email("test@example.com")
                .password("password")
                .role(Role.EMPLOYEE)
                .sectionId(1L)
                .active(true)
                .build();
        var auth = new UsernamePasswordAuthenticationToken(
                employee, null, List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private NotificationResponse createResponse(Long id, NotificationType type) {
        return new NotificationResponse(id, 1L, type, "テスト通知", "テストメッセージ",
                false, LocalDateTime.now(), null);
    }
}
