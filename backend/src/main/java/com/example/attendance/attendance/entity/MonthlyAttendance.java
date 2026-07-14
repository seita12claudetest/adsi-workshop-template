package com.example.attendance.attendance.entity;

import com.example.attendance.common.enums.MonthlyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_attendances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "year_month", nullable = false)
    private String yearMonth;

    @Column(name = "total_working_minutes", nullable = false)
    private int totalWorkingMinutes;

    @Column(name = "total_overtime_minutes", nullable = false)
    private int totalOvertimeMinutes;

    @Column(name = "working_days", nullable = false)
    private int workingDays;

    @Column(name = "paid_leave_days", nullable = false)
    private BigDecimal paidLeaveDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MonthlyStatus status;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
