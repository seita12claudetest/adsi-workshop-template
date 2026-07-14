package com.example.attendance.attendance.dto;

import com.example.attendance.attendance.vo.ClockState;
import com.example.attendance.common.enums.TimeRecordType;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record TimeRecordStatusResponse(
        ClockState currentState,
        Set<TimeRecordType> nextActions,
        LocalTime clockInAt,
        Integer elapsedMinutes,
        Integer breakMinutesToday,
        List<RecordEntry> todayRecords
) {
    public record RecordEntry(
            TimeRecordType type,
            LocalTime recordedAt
    ) {
    }
}
