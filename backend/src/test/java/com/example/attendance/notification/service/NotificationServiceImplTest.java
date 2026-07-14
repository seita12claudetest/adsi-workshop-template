package com.example.attendance.notification.service;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.UnreadCountResponse;
import com.example.attendance.notification.entity.Notification;
import com.example.attendance.notification.repository.NotificationRepository;
import com.example.attendance.notification.sse.NotificationSseEmitterManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSseEmitterManager sseEmitterManager;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    @DisplayName("通知一覧取得: ページネーションされたリストが返される")
    void getNotifications_returnsPagedList() {
        Pageable pageable = PageRequest.of(0, 10);
        Notification notification = createNotification(1L, 100L, NotificationType.APPROVAL_REQUEST);
        Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(100L, pageable))
                .thenReturn(page);

        Page<NotificationResponse> result = notificationService.getNotifications(100L, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).type()).isEqualTo(NotificationType.APPROVAL_REQUEST);
    }

    @Test
    @DisplayName("通知一覧取得（タイプフィルタ）: 指定タイプのみ返される")
    void getNotifications_withTypeFilter_returnsFilteredList() {
        Pageable pageable = PageRequest.of(0, 10);
        Notification notification = createNotification(1L, 100L, NotificationType.CLOCK_REMINDER);
        Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

        when(notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(
                100L, NotificationType.CLOCK_REMINDER, pageable))
                .thenReturn(page);

        Page<NotificationResponse> result = notificationService.getNotifications(
                100L, NotificationType.CLOCK_REMINDER, pageable);

        assertThat(result.getContent().get(0).type()).isEqualTo(NotificationType.CLOCK_REMINDER);
    }

    @Test
    @DisplayName("未読数取得: 正しいカウントが返される")
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByRecipientIdAndReadFalse(100L)).thenReturn(5L);

        UnreadCountResponse result = notificationService.getUnreadCount(100L);

        assertThat(result.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("個別既読: 存在する通知を既読にする")
    void markAsRead_existingNotification_marksRead() {
        Notification notification = createNotification(1L, 100L, NotificationType.APPROVAL_REQUEST);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenReturn(notification);

        NotificationResponse result = notificationService.markAsRead(1L, 100L);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("個別既読: 存在しない通知で例外")
    void markAsRead_notFound_throwsException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(999L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("個別既読: 他人の通知は既読にできない")
    void markAsRead_otherRecipient_throwsException() {
        Notification notification = createNotification(1L, 200L, NotificationType.APPROVAL_REQUEST);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("全件既読: 該当受信者の全未読を既読にする")
    void markAllAsRead_marksAllUnreadForRecipient() {
        when(notificationRepository.markAllAsReadByRecipientId(100L)).thenReturn(3);

        notificationService.markAllAsRead(100L);

        verify(notificationRepository).markAllAsReadByRecipientId(100L);
    }

    @Test
    @DisplayName("一括既読: 指定IDの通知を既読にする")
    void markBatchAsRead_marksSpecifiedNotifications() {
        List<Long> ids = List.of(1L, 2L, 3L);
        Notification n1 = createNotification(1L, 100L, NotificationType.APPROVAL_REQUEST);
        Notification n2 = createNotification(2L, 100L, NotificationType.APPROVAL_RESULT);
        Notification n3 = createNotification(3L, 100L, NotificationType.CLOCK_REMINDER);

        when(notificationRepository.findAllById(ids)).thenReturn(List.of(n1, n2, n3));

        notificationService.markBatchAsRead(ids, 100L);

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
        assertThat(n3.isRead()).isTrue();
        verify(notificationRepository).saveAll(any());
    }

    @Test
    @DisplayName("通知作成: 保存してSSEで配信される")
    void createNotification_savesAndPublishesSse() {
        Notification notification = createNotification(null, 100L, NotificationType.APPROVAL_REQUEST);
        Notification saved = createNotification(1L, 100L, NotificationType.APPROVAL_REQUEST);
        when(notificationRepository.save(notification)).thenReturn(saved);

        Notification result = notificationService.createNotification(notification);

        assertThat(result.getId()).isEqualTo(1L);
        verify(sseEmitterManager).send(eq(100L), any(NotificationResponse.class));
    }

    private Notification createNotification(Long id, Long recipientId, NotificationType type) {
        return Notification.builder()
                .id(id)
                .recipientId(recipientId)
                .type(type)
                .title("テスト通知")
                .message("テストメッセージ")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
