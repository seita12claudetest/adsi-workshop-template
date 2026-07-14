package com.example.attendance.attendance.service;

import com.example.attendance.attendance.entity.TimeRecord;
import com.example.attendance.common.enums.TimeRecordType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 14);

    @Test
    @DisplayName("通常勤務: 9:15-17:30, 休憩60分 → 実労働435分, 残業0分")
    void normalDay_noOvertime() {
        var records = List.of(
                record(TimeRecordType.BREAK_START, 12, 0),
                record(TimeRecordType.BREAK_END, 13, 0)
        );
        int breakMinutes = AttendanceCalculator.calculateBreakMinutes(records);
        int working = AttendanceCalculator.calculateWorkingMinutes(
                LocalTime.of(9, 15), LocalTime.of(17, 30), breakMinutes);
        int overtime = AttendanceCalculator.calculateOvertimeMinutes(working);

        assertThat(breakMinutes).isEqualTo(60);
        assertThat(working).isEqualTo(435);
        assertThat(overtime).isEqualTo(0);
    }

    @Test
    @DisplayName("残業あり: 9:00-19:00, 休憩60分 → 実労働540分, 残業105分")
    void overtime_calculatedCorrectly() {
        var records = List.of(
                record(TimeRecordType.BREAK_START, 12, 0),
                record(TimeRecordType.BREAK_END, 13, 0)
        );
        int breakMinutes = AttendanceCalculator.calculateBreakMinutes(records);
        int working = AttendanceCalculator.calculateWorkingMinutes(
                LocalTime.of(9, 0), LocalTime.of(19, 0), breakMinutes);
        int overtime = AttendanceCalculator.calculateOvertimeMinutes(working);

        assertThat(working).isEqualTo(540);
        assertThat(overtime).isEqualTo(105);
    }

    @Test
    @DisplayName("複数休憩: 2回の休憩合計90分 → 正しく減算")
    void multipleBreaks_summedCorrectly() {
        var records = List.of(
                record(TimeRecordType.BREAK_START, 12, 0),
                record(TimeRecordType.BREAK_END, 13, 0),
                record(TimeRecordType.BREAK_START, 15, 0),
                record(TimeRecordType.BREAK_END, 15, 30)
        );
        int breakMinutes = AttendanceCalculator.calculateBreakMinutes(records);
        int working = AttendanceCalculator.calculateWorkingMinutes(
                LocalTime.of(9, 0), LocalTime.of(18, 0), breakMinutes);

        assertThat(breakMinutes).isEqualTo(90);
        assertThat(working).isEqualTo(450);
    }

    @Test
    @DisplayName("休憩未打刻: 所定60分で計算")
    void noBreakRecords_defaultBreakApplied() {
        List<TimeRecord> records = List.of();
        int breakMinutes = AttendanceCalculator.calculateBreakMinutes(records);
        int working = AttendanceCalculator.calculateWorkingMinutes(
                LocalTime.of(9, 0), LocalTime.of(18, 0), breakMinutes);

        assertThat(breakMinutes).isEqualTo(60);
        assertThat(working).isEqualTo(480);
    }

    @Test
    @DisplayName("出退勤がnullの場合は実労働0分")
    void nullClockTimes_zeroWorking() {
        assertThat(AttendanceCalculator.calculateWorkingMinutes(null, LocalTime.of(17, 0), 60)).isEqualTo(0);
        assertThat(AttendanceCalculator.calculateWorkingMinutes(LocalTime.of(9, 0), null, 60)).isEqualTo(0);
    }

    private TimeRecord record(TimeRecordType type, int hour, int minute) {
        return TimeRecord.builder()
                .type(type)
                .recordedAt(LocalDateTime.of(TODAY, LocalTime.of(hour, minute)))
                .date(TODAY)
                .build();
    }
}
