package com.example.attendance.application.entity;

import com.example.attendance.common.enums.OvertimeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "overtime_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false, unique = true)
    private Long applicationId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "expected_minutes", nullable = false)
    private Integer expectedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "overtime_type", nullable = false)
    private OvertimeType overtimeType;
}
