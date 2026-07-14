package com.example.attendance.notification.event;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.entity.Notification;
import com.example.attendance.notification.factory.NotificationFactory;
import com.example.attendance.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationFactory notificationFactory;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    @DisplayName("承認依頼イベント: 通知を生成して保存する")
    void onNotificationEvent_approvalRequest_savesNotification() {
        NotificationEvent event = NotificationEvent.approvalRequest(10L, 100L, "田中太郎", "休暇申請");
        Notification notification = Notification.builder()
                .recipientId(10L)
                .type(NotificationType.APPROVAL_REQUEST)
                .title("承認依頼")
                .message("テスト")
                .build();
        when(notificationFactory.createApprovalRequest(10L, 100L, "田中太郎", "休暇申請"))
                .thenReturn(notification);
        when(notificationService.createNotification(any())).thenReturn(notification);

        listener.handleNotificationEvent(event);

        verify(notificationFactory).createApprovalRequest(10L, 100L, "田中太郎", "休暇申請");
        verify(notificationService).createNotification(notification);
    }

    @Test
    @DisplayName("承認結果イベント: 通知を生成して保存する")
    void onNotificationEvent_approvalResult_savesNotification() {
        NotificationEvent event = NotificationEvent.approvalResult(20L, 100L, "休暇申請", true);
        Notification notification = Notification.builder()
                .recipientId(20L)
                .type(NotificationType.APPROVAL_RESULT)
                .title("承認")
                .message("テスト")
                .build();
        when(notificationFactory.createApprovalResult(20L, 100L, "休暇申請", true))
                .thenReturn(notification);
        when(notificationService.createNotification(any())).thenReturn(notification);

        listener.handleNotificationEvent(event);

        verify(notificationFactory).createApprovalResult(20L, 100L, "休暇申請", true);
        verify(notificationService).createNotification(notification);
    }

    @Test
    @DisplayName("有給残アラートイベント: 通知を生成して保存する")
    void onNotificationEvent_leaveBalanceAlert_savesNotification() {
        NotificationEvent event = NotificationEvent.leaveBalanceAlert(30L, new BigDecimal("2.5"));
        Notification notification = Notification.builder()
                .recipientId(30L)
                .type(NotificationType.LEAVE_BALANCE_ALERT)
                .title("有給残")
                .message("テスト")
                .build();
        when(notificationFactory.createLeaveBalanceAlert(30L, new BigDecimal("2.5")))
                .thenReturn(notification);
        when(notificationService.createNotification(any())).thenReturn(notification);

        listener.handleNotificationEvent(event);

        verify(notificationFactory).createLeaveBalanceAlert(30L, new BigDecimal("2.5"));
        verify(notificationService).createNotification(notification);
    }

    @Test
    @DisplayName("打刻リマインドイベント: 通知を生成して保存する")
    void onNotificationEvent_clockReminder_savesNotification() {
        NotificationEvent event = NotificationEvent.clockReminder(40L);
        Notification notification = Notification.builder()
                .recipientId(40L)
                .type(NotificationType.CLOCK_REMINDER)
                .title("打刻リマインド")
                .message("テスト")
                .build();
        when(notificationFactory.createClockReminder(40L)).thenReturn(notification);
        when(notificationService.createNotification(any())).thenReturn(notification);

        listener.handleNotificationEvent(event);

        verify(notificationFactory).createClockReminder(40L);
        verify(notificationService).createNotification(notification);
    }
}
