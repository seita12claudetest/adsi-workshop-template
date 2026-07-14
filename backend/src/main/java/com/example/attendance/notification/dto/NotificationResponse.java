package com.example.attendance.notification.dto;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.entity.Notification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long recipientId,
        NotificationType type,
        String title,
        String message,
        boolean read,
        LocalDateTime createdAt,
        Long relatedApplicationId
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getRelatedApplicationId()
        );
    }
}
