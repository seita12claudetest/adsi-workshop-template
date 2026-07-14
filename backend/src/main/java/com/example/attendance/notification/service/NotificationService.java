package com.example.attendance.notification.service;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.UnreadCountResponse;
import com.example.attendance.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    Page<NotificationResponse> getNotifications(Long recipientId, NotificationType type, Pageable pageable);

    UnreadCountResponse getUnreadCount(Long recipientId);

    NotificationResponse markAsRead(Long notificationId, Long recipientId);

    void markAllAsRead(Long recipientId);

    void markBatchAsRead(List<Long> ids, Long recipientId);

    Notification createNotification(Notification notification);
}
