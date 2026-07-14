package com.example.attendance.notification.factory;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.entity.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationFactoryTest {

    private final NotificationFactory factory = new NotificationFactory();

    @Test
    @DisplayName("承認依頼通知: 正しいタイトルとメッセージが生成される")
    void createApprovalRequest_generatesCorrectTitleAndMessage() {
        Notification notification = factory.createApprovalRequest(
                10L, 100L, "田中太郎", "休暇申請");

        assertThat(notification.getRecipientId()).isEqualTo(10L);
        assertThat(notification.getType()).isEqualTo(NotificationType.APPROVAL_REQUEST);
        assertThat(notification.getTitle()).contains("承認依頼");
        assertThat(notification.getMessage()).contains("田中太郎");
        assertThat(notification.getMessage()).contains("休暇申請");
        assertThat(notification.getRelatedApplicationId()).isEqualTo(100L);
        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("承認結果通知（承認）: 正しいメッセージが生成される")
    void createApprovalResult_approved_generatesMessage() {
        Notification notification = factory.createApprovalResult(
                20L, 100L, "休暇申請", true);

        assertThat(notification.getRecipientId()).isEqualTo(20L);
        assertThat(notification.getType()).isEqualTo(NotificationType.APPROVAL_RESULT);
        assertThat(notification.getTitle()).contains("承認");
        assertThat(notification.getMessage()).contains("承認されました");
        assertThat(notification.getRelatedApplicationId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("承認結果通知（差戻）: 正しいメッセージが生成される")
    void createApprovalResult_rejected_generatesMessage() {
        Notification notification = factory.createApprovalResult(
                20L, 100L, "休暇申請", false);

        assertThat(notification.getType()).isEqualTo(NotificationType.APPROVAL_RESULT);
        assertThat(notification.getTitle()).contains("差戻");
        assertThat(notification.getMessage()).contains("差し戻されました");
    }

    @Test
    @DisplayName("有給残アラート: 残日数が含まれる")
    void createLeaveBalanceAlert_includesRemainingDays() {
        Notification notification = factory.createLeaveBalanceAlert(
                30L, new BigDecimal("2.5"));

        assertThat(notification.getRecipientId()).isEqualTo(30L);
        assertThat(notification.getType()).isEqualTo(NotificationType.LEAVE_BALANCE_ALERT);
        assertThat(notification.getTitle()).contains("有給");
        assertThat(notification.getMessage()).contains("2.5");
        assertThat(notification.getRelatedApplicationId()).isNull();
    }

    @Test
    @DisplayName("打刻忘れリマインド: 正しいメッセージが生成される")
    void createClockReminder_generatesMessage() {
        Notification notification = factory.createClockReminder(40L);

        assertThat(notification.getRecipientId()).isEqualTo(40L);
        assertThat(notification.getType()).isEqualTo(NotificationType.CLOCK_REMINDER);
        assertThat(notification.getTitle()).contains("打刻");
        assertThat(notification.getMessage()).contains("退勤");
        assertThat(notification.getRelatedApplicationId()).isNull();
    }
}
