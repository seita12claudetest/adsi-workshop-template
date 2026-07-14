package com.example.attendance.notification.service;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.notification.dto.NotificationResponse;
import com.example.attendance.notification.dto.UnreadCountResponse;
import com.example.attendance.notification.entity.Notification;
import com.example.attendance.notification.repository.NotificationRepository;
import com.example.attendance.notification.sse.NotificationSseEmitterManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSseEmitterManager sseEmitterManager;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationSseEmitterManager sseEmitterManager) {
        this.notificationRepository = notificationRepository;
        this.sseEmitterManager = sseEmitterManager;
    }

    @Override
    public Page<NotificationResponse> getNotifications(Long recipientId, NotificationType type, Pageable pageable) {
        Page<Notification> page;
        if (type != null) {
            page = notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(recipientId, type, pageable);
        } else {
            page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
        }
        return page.map(NotificationResponse::from);
    }

    @Override
    public UnreadCountResponse getUnreadCount(Long recipientId) {
        long count = notificationRepository.countByRecipientIdAndReadFalse(recipientId);
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("通知", notificationId));

        if (!notification.getRecipientId().equals(recipientId)) {
            throw new ResourceNotFoundException("通知", notificationId);
        }

        notification.setRead(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(Long recipientId) {
        notificationRepository.markAllAsReadByRecipientId(recipientId);
    }

    @Override
    @Transactional
    public void markBatchAsRead(List<Long> ids, Long recipientId) {
        List<Notification> notifications = notificationRepository.findAllById(ids);
        List<Notification> owned = notifications.stream()
                .filter(n -> n.getRecipientId().equals(recipientId))
                .toList();
        owned.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(owned);
    }

    @Override
    @Transactional
    public Notification createNotification(Notification notification) {
        Notification saved = notificationRepository.save(notification);
        sseEmitterManager.send(saved.getRecipientId(), NotificationResponse.from(saved));
        return saved;
    }
}
