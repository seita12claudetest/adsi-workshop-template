package com.example.attendance.application.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "time_correction_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeCorrectionApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false, unique = true)
    private Long applicationId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "original_clock_in")
    private LocalTime originalClockIn;

    @Column(name = "original_clock_out")
    private LocalTime originalClockOut;

    @Column(name = "corrected_clock_in")
    private LocalTime correctedClockIn;

    @Column(name = "corrected_clock_out")
    private LocalTime correctedClockOut;
}
