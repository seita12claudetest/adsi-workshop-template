package com.example.attendance.notification.event;

import com.example.attendance.notification.entity.Notification;
import com.example.attendance.notification.factory.NotificationFactory;
import com.example.attendance.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {

    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;

    public NotificationEventListener(NotificationFactory notificationFactory,
                                     NotificationService notificationService) {
        this.notificationFactory = notificationFactory;
        this.notificationService = notificationService;
    }

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("通知イベント受信: type={}, recipientId={}", event.type(), event.recipientId());

        Notification notification = switch (event.type()) {
            case APPROVAL_REQUEST -> notificationFactory.createApprovalRequest(
                    event.recipientId(), event.applicationId(),
                    event.applicantName(), event.applicationType());
            case APPROVAL_RESULT -> notificationFactory.createApprovalResult(
                    event.recipientId(), event.applicationId(),
                    event.applicationType(), event.approved());
            case LEAVE_BALANCE_ALERT -> notificationFactory.createLeaveBalanceAlert(
                    event.recipientId(), event.remainingDays());
            case CLOCK_REMINDER -> notificationFactory.createClockReminder(event.recipientId());
        };

        notificationService.createNotification(notification);
    }
}
