package com.example.attendance.application.entity;

import com.example.attendance.common.enums.ApprovalAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "approvals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "approver_id", nullable = false)
    private Long approverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalAction action;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        if (decidedAt == null) {
            decidedAt = LocalDateTime.now();
        }
    }
}
