package com.example.attendance.attendance.service;

import com.example.attendance.attendance.entity.TimeRecord;
import com.example.attendance.attendance.vo.WorkingTime;
import com.example.attendance.common.enums.TimeRecordType;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class AttendanceCalculator {

    private AttendanceCalculator() {
    }

    public static int calculateBreakMinutes(List<TimeRecord> records) {
        var breakStarts = records.stream()
                .filter(r -> r.getType() == TimeRecordType.BREAK_START)
                .map(r -> r.getRecordedAt().toLocalTime())
                .sorted()
                .toList();

        var breakEnds = records.stream()
                .filter(r -> r.getType() == TimeRecordType.BREAK_END)
                .map(r -> r.getRecordedAt().toLocalTime())
                .sorted()
                .toList();

        int totalBreak = 0;
        for (int i = 0; i < breakStarts.size(); i++) {
            LocalTime start = breakStarts.get(i);
            LocalTime end = i < breakEnds.size() ? breakEnds.get(i) : null;
            if (end != null) {
                totalBreak += (int) ChronoUnit.MINUTES.between(start, end);
            }
        }

        return totalBreak > 0 ? totalBreak : WorkingTime.DEFAULT_BREAK_MINUTES;
    }

    public static int calculateBreakMinutesStrict(List<TimeRecord> records) {
        var breakStarts = records.stream()
                .filter(r -> r.getType() == TimeRecordType.BREAK_START)
                .map(r -> r.getRecordedAt().toLocalTime())
                .sorted()
                .toList();

        var breakEnds = records.stream()
                .filter(r -> r.getType() == TimeRecordType.BREAK_END)
                .map(r -> r.getRecordedAt().toLocalTime())
                .sorted()
                .toList();

        int totalBreak = 0;
        for (int i = 0; i < breakStarts.size(); i++) {
            LocalTime start = breakStarts.get(i);
            LocalTime end = i < breakEnds.size() ? breakEnds.get(i) : null;
            if (end != null) {
                totalBreak += (int) ChronoUnit.MINUTES.between(start, end);
            }
        }

        return totalBreak;
    }

    public static boolean hasBreakRecords(List<TimeRecord> records) {
        return records.stream().anyMatch(r -> r.getType() == TimeRecordType.BREAK_START);
    }

    public static int calculateWorkingMinutes(LocalTime clockIn, LocalTime clockOut, int breakMinutes) {
        if (clockIn == null || clockOut == null) {
            return 0;
        }
        int totalMinutes = (int) ChronoUnit.MINUTES.between(clockIn, clockOut);
        return Math.max(0, totalMinutes - breakMinutes);
    }

    public static int calculateOvertimeMinutes(int workingMinutes) {
        return Math.max(0, workingMinutes - WorkingTime.STANDARD_WORKING_MINUTES);
    }
}
