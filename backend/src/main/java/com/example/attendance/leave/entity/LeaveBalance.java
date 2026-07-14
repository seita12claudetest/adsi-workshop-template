package com.example.attendance.leave.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "granted_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal grantedDays;

    @Column(name = "used_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal usedDays;

    @Column(name = "remaining_days", nullable = false, precision = 4, scale = 1)
    private BigDecimal remainingDays;

    @Column(name = "grant_date", nullable = false)
    private LocalDate grantDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

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

    public boolean isExpired(LocalDate today) {
        return !today.isBefore(expiryDate.plusDays(1));
    }

    public boolean hasRemaining() {
        return remainingDays.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal consume(BigDecimal days) {
        BigDecimal actual = days.min(remainingDays);
        usedDays = usedDays.add(actual);
        remainingDays = remainingDays.subtract(actual);
        return actual;
    }
}
