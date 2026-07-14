package com.example.attendance.notification.event;

import com.example.attendance.common.enums.NotificationType;

import java.math.BigDecimal;

public record NotificationEvent(
        NotificationType type,
        Long recipientId,
        Long applicationId,
        String applicantName,
        String applicationType,
        Boolean approved,
        BigDecimal remainingDays
) {

    public static NotificationEvent approvalRequest(Long recipientId, Long applicationId,
                                                    String applicantName, String applicationType) {
        return new NotificationEvent(NotificationType.APPROVAL_REQUEST,
                recipientId, applicationId, applicantName, applicationType, null, null);
    }

    public static NotificationEvent approvalResult(Long recipientId, Long applicationId,
                                                   String applicationType, boolean approved) {
        return new NotificationEvent(NotificationType.APPROVAL_RESULT,
                recipientId, applicationId, null, applicationType, approved, null);
    }

    public static NotificationEvent leaveBalanceAlert(Long recipientId, BigDecimal remainingDays) {
        return new NotificationEvent(NotificationType.LEAVE_BALANCE_ALERT,
                recipientId, null, null, null, null, remainingDays);
    }

    public static NotificationEvent clockReminder(Long recipientId) {
        return new NotificationEvent(NotificationType.CLOCK_REMINDER,
                recipientId, null, null, null, null, null);
    }
}
