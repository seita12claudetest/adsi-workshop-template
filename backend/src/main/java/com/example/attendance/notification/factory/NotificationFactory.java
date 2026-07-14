package com.example.attendance.notification.factory;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.notification.entity.Notification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NotificationFactory {

    public Notification createApprovalRequest(Long recipientId, Long applicationId,
                                              String applicantName, String applicationType) {
        return Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.APPROVAL_REQUEST)
                .title("承認依頼: " + applicationType)
                .message(applicantName + "さんから「" + applicationType + "」の承認依頼があります。")
                .read(false)
                .relatedApplicationId(applicationId)
                .build();
    }

    public Notification createApprovalResult(Long recipientId, Long applicationId,
                                             String applicationType, boolean approved) {
        String action = approved ? "承認" : "差戻";
        String resultMessage = approved ? "承認されました" : "差し戻されました";

        return Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.APPROVAL_RESULT)
                .title(action + ": " + applicationType)
                .message("あなたの「" + applicationType + "」が" + resultMessage + "。")
                .read(false)
                .relatedApplicationId(applicationId)
                .build();
    }

    public Notification createLeaveBalanceAlert(Long recipientId, BigDecimal remainingDays) {
        return Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.LEAVE_BALANCE_ALERT)
                .title("有給残日数アラート")
                .message("有給休暇の残日数が" + remainingDays + "日になりました。計画的に取得してください。")
                .read(false)
                .relatedApplicationId(null)
                .build();
    }

    public Notification createClockReminder(Long recipientId) {
        return Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.CLOCK_REMINDER)
                .title("打刻リマインド")
                .message("本日の退勤打刻がまだ行われていません。退勤打刻を忘れずにお願いします。")
                .read(false)
                .relatedApplicationId(null)
                .build();
    }
}
