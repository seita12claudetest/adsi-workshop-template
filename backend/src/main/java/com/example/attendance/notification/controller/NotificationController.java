package com.example.attendance.notification.controller;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.ReadBatchRequest;
import com.example.attendance.notification.dto.UnreadCountResponse;
import com.example.attendance.notification.service.NotificationService;
import com.example.attendance.notification.sse.NotificationSseEmitterManager;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSseEmitterManager sseEmitterManager;

    public NotificationController(NotificationService notificationService,
                                  NotificationSseEmitterManager sseEmitterManager) {
        this.notificationService = notificationService;
        this.sseEmitterManager = sseEmitterManager;
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Employee employee,
            @RequestParam(required = false) NotificationType type,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(employee.getId(), type, pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(notificationService.getUnreadCount(employee.getId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal Employee employee) {
        return ResponseEntity.ok(notificationService.markAsRead(id, employee.getId()));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal Employee employee) {
        notificationService.markAllAsRead(employee.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-batch")
    public ResponseEntity<Void> markBatchAsRead(
            @Valid @RequestBody ReadBatchRequest request,
            @AuthenticationPrincipal Employee employee) {
        notificationService.markBatchAsRead(request.ids(), employee.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal Employee employee) {
        return sseEmitterManager.createEmitter(employee.getId());
    }
}
