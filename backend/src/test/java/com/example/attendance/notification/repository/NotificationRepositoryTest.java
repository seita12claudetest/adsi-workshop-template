package com.example.attendance.notification.repository;

import com.example.attendance.common.enums.NotificationType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.notification.entity.Notification;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    private Long recipientId;
    private Long otherRecipientId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        employeeRepository.deleteAll();

        // 組織構造を作成（FK制約対応）
        entityManager.createNativeQuery(
                "MERGE INTO organizations (id, name, code, created_at, updated_at) " +
                "KEY(id) VALUES (1, 'テスト本部', 'TEST', NOW(), NOW())").executeUpdate();
        entityManager.createNativeQuery(
                "MERGE INTO departments (id, organization_id, name, code, created_at, updated_at) " +
                "KEY(id) VALUES (1, 1, 'テスト部', 'DEPT1', NOW(), NOW())").executeUpdate();
        entityManager.createNativeQuery(
                "MERGE INTO sections (id, department_id, name, code, created_at, updated_at) " +
                "KEY(id) VALUES (1, 1, 'テスト課', 'SEC1', NOW(), NOW())").executeUpdate();
        entityManager.flush();

        Employee recipient = employeeRepository.save(Employee.builder()
                .employeeCode("EMP001")
                .name("テスト太郎")
                .email("test1@example.com")
                .password("password")
                .role(Role.EMPLOYEE)
                .sectionId(1L)
                .hireDate(LocalDate.of(2020, 4, 1))
                .active(true)
                .build());
        recipientId = recipient.getId();

        Employee other = employeeRepository.save(Employee.builder()
                .employeeCode("EMP002")
                .name("テスト次郎")
                .email("test2@example.com")
                .password("password")
                .role(Role.EMPLOYEE)
                .sectionId(1L)
                .hireDate(LocalDate.of(2020, 4, 1))
                .active(true)
                .build());
        otherRecipientId = other.getId();

        notificationRepository.save(Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.APPROVAL_REQUEST)
                .title("承認依頼")
                .message("テスト通知1")
                .read(false)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build());

        notificationRepository.save(Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.APPROVAL_RESULT)
                .title("承認結果")
                .message("テスト通知2")
                .read(true)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build());

        notificationRepository.save(Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.CLOCK_REMINDER)
                .title("打刻リマインド")
                .message("テスト通知3")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build());

        notificationRepository.save(Notification.builder()
                .recipientId(otherRecipientId)
                .type(NotificationType.CLOCK_REMINDER)
                .title("他人の通知")
                .message("別ユーザー")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("受信者IDで通知一覧を取得: 作成日時降順でページネーション")
    void findByRecipientIdOrderByCreatedAtDesc_returnsNotifications() {
        Page<Notification> page = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent().get(0).getType()).isEqualTo(NotificationType.CLOCK_REMINDER);
    }

    @Test
    @DisplayName("受信者IDとタイプでフィルタ: 指定タイプのみ返される")
    void findByRecipientIdAndTypeOrderByCreatedAtDesc_filtersCorrectly() {
        Page<Notification> page = notificationRepository
                .findByRecipientIdAndTypeOrderByCreatedAtDesc(
                        recipientId, NotificationType.APPROVAL_REQUEST, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("承認依頼");
    }

    @Test
    @DisplayName("未読数取得: 該当受信者の未読のみカウント")
    void countByRecipientIdAndReadFalse_returnsUnreadCount() {
        long count = notificationRepository.countByRecipientIdAndReadFalse(recipientId);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("全既読化: 該当受信者の未読をすべて既読にする")
    void markAllAsReadByRecipientId_marksAllUnread() {
        int updated = notificationRepository.markAllAsReadByRecipientId(recipientId);

        assertThat(updated).isEqualTo(2);
        assertThat(notificationRepository.countByRecipientIdAndReadFalse(recipientId)).isZero();
        // 他ユーザーは影響なし
        assertThat(notificationRepository.countByRecipientIdAndReadFalse(otherRecipientId)).isEqualTo(1);
    }
}
