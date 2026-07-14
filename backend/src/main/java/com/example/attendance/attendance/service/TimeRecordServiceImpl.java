package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.TimeRecordRequest;
import com.example.attendance.attendance.dto.TimeRecordResponse;
import com.example.attendance.attendance.dto.TimeRecordStatusResponse;
import com.example.attendance.attendance.entity.DailyAttendance;
import com.example.attendance.attendance.entity.TimeRecord;
import com.example.attendance.attendance.repository.DailyAttendanceRepository;
import com.example.attendance.attendance.repository.MonthlyAttendanceRepository;
import com.example.attendance.attendance.repository.TimeRecordRepository;
import com.example.attendance.attendance.vo.ClockState;
import com.example.attendance.common.enums.DailyStatus;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TimeRecordServiceImpl implements TimeRecordService {

    private final TimeRecordRepository timeRecordRepository;
    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final MonthlyAttendanceRepository monthlyAttendanceRepository;
    private final Clock clock;

    public TimeRecordServiceImpl(TimeRecordRepository timeRecordRepository,
                                 DailyAttendanceRepository dailyAttendanceRepository,
                                 MonthlyAttendanceRepository monthlyAttendanceRepository,
                                 Clock clock) {
        this.timeRecordRepository = timeRecordRepository;
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.monthlyAttendanceRepository = monthlyAttendanceRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public TimeRecordResponse record(Long employeeId, TimeRecordRequest request) {
        LocalDate today = LocalDate.now(clock);
        List<TimeRecord> todayRecords = timeRecordRepository
                .findByEmployeeIdAndDateOrderByRecordedAtAsc(employeeId, today);

        ClockState currentState = deriveState(todayRecords);

        if (!currentState.canTransition(request.type())) {
            throw new BusinessException(buildErrorMessage(currentState, request.type()));
        }

        LocalDateTime now = LocalDateTime.now(clock);
        TimeRecord record = TimeRecord.builder()
                .employeeId(employeeId)
                .type(request.type())
                .recordedAt(now)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .withinArea(false)
                .date(today)
                .build();

        TimeRecord saved = timeRecordRepository.save(record);

        updateDailyAttendance(employeeId, today, request.type(), now.toLocalTime(), todayRecords);

        return new TimeRecordResponse(
                saved.getId(),
                saved.getType(),
                saved.getRecordedAt(),
                saved.isWithinArea(),
                null,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeRecordResponse> getByDate(Long employeeId, LocalDate date) {
        return timeRecordRepository.findByEmployeeIdAndDateOrderByRecordedAtAsc(employeeId, date)
                .stream()
                .map(r -> new TimeRecordResponse(
                        r.getId(), r.getType(), r.getRecordedAt(),
                        r.isWithinArea(), null, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TimeRecordStatusResponse getStatus(Long employeeId) {
        LocalDate today = LocalDate.now(clock);
        List<TimeRecord> todayRecords = timeRecordRepository
                .findByEmployeeIdAndDateOrderByRecordedAtAsc(employeeId, today);

        ClockState state = deriveState(todayRecords);

        LocalTime clockInAt = todayRecords.stream()
                .filter(r -> r.getType() == TimeRecordType.CLOCK_IN)
                .findFirst()
                .map(r -> r.getRecordedAt().toLocalTime())
                .orElse(null);

        Integer elapsedMinutes = null;
        if (clockInAt != null && state != ClockState.CLOCKED_OUT) {
            elapsedMinutes = (int) ChronoUnit.MINUTES.between(clockInAt, LocalTime.now(clock));
        }

        int breakMinutes = AttendanceCalculator.calculateBreakMinutesStrict(todayRecords);

        List<TimeRecordStatusResponse.RecordEntry> entries = todayRecords.stream()
                .map(r -> new TimeRecordStatusResponse.RecordEntry(
                        r.getType(), r.getRecordedAt().toLocalTime()))
                .toList();

        return new TimeRecordStatusResponse(
                state,
                state.getAllowedTransitions(),
                clockInAt,
                elapsedMinutes,
                breakMinutes,
                entries
        );
    }

    private ClockState deriveState(List<TimeRecord> records) {
        ClockState state = ClockState.NOT_CLOCKED_IN;
        for (TimeRecord record : records) {
            state = state.next(record.getType());
        }
        return state;
    }

    private void updateDailyAttendance(Long employeeId, LocalDate date,
                                       TimeRecordType type, LocalTime time,
                                       List<TimeRecord> existingRecords) {
        DailyAttendance daily = dailyAttendanceRepository.findByEmployeeIdAndDate(employeeId, date)
                .orElse(null);

        switch (type) {
            case CLOCK_IN -> {
                if (daily == null) {
                    daily = DailyAttendance.builder()
                            .employeeId(employeeId)
                            .date(date)
                            .clockIn(time)
                            .status(DailyStatus.NORMAL)
                            .build();
                    dailyAttendanceRepository.save(daily);
                }
            }
            case BREAK_START -> {
                if (daily != null && daily.getBreakStart() == null) {
                    daily.setBreakStart(time);
                    dailyAttendanceRepository.save(daily);
                }
            }
            case BREAK_END -> {
                if (daily != null) {
                    daily.setBreakEnd(time);
                    dailyAttendanceRepository.save(daily);
                }
            }
            case CLOCK_OUT -> {
                if (daily != null) {
                    daily.setClockOut(time);

                    List<TimeRecord> allRecords = new java.util.ArrayList<>(existingRecords);
                    allRecords.add(TimeRecord.builder().type(type)
                            .recordedAt(LocalDateTime.of(date, time)).date(date).build());

                    int breakMinutes = AttendanceCalculator.hasBreakRecords(allRecords)
                            ? AttendanceCalculator.calculateBreakMinutesStrict(allRecords)
                            : AttendanceCalculator.calculateBreakMinutes(allRecords);

                    int workingMinutes = AttendanceCalculator.calculateWorkingMinutes(
                            daily.getClockIn(), time, breakMinutes);
                    int overtimeMinutes = AttendanceCalculator.calculateOvertimeMinutes(workingMinutes);

                    daily.setBreakMinutes(breakMinutes);
                    daily.setWorkingMinutes(workingMinutes);
                    daily.setOvertimeMinutes(overtimeMinutes);
                    dailyAttendanceRepository.save(daily);

                    updateMonthlyAttendance(employeeId, date);
                }
            }
        }
    }

    private void updateMonthlyAttendance(Long employeeId, LocalDate date) {
        String yearMonth = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());

        List<DailyAttendance> dailies = dailyAttendanceRepository
                .findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, startOfMonth, endOfMonth);

        int totalWorking = dailies.stream()
                .filter(d -> d.getWorkingMinutes() != null)
                .mapToInt(DailyAttendance::getWorkingMinutes)
                .sum();
        int totalOvertime = dailies.stream()
                .filter(d -> d.getOvertimeMinutes() != null)
                .mapToInt(DailyAttendance::getOvertimeMinutes)
                .sum();
        int workingDays = (int) dailies.stream()
                .filter(d -> d.getWorkingMinutes() != null && d.getWorkingMinutes() > 0)
                .count();

        var monthly = monthlyAttendanceRepository
                .findByEmployeeIdAndYearMonth(employeeId, yearMonth)
                .orElse(com.example.attendance.attendance.entity.MonthlyAttendance.builder()
                        .employeeId(employeeId)
                        .yearMonth(yearMonth)
                        .paidLeaveDays(java.math.BigDecimal.ZERO)
                        .status(com.example.attendance.common.enums.MonthlyStatus.OPEN)
                        .build());

        monthly.setTotalWorkingMinutes(totalWorking);
        monthly.setTotalOvertimeMinutes(totalOvertime);
        monthly.setWorkingDays(workingDays);
        monthlyAttendanceRepository.save(monthly);
    }

    private String buildErrorMessage(ClockState currentState, TimeRecordType requestedType) {
        return switch (currentState) {
            case NOT_CLOCKED_IN -> "出勤打刻がありません";
            case WORKING -> requestedType == TimeRecordType.CLOCK_IN ? "既に出勤済みです" : "不正な打刻です";
            case ON_BREAK -> "休憩中です。先に休憩終了してください";
            case CLOCKED_OUT -> "既に退勤済みです";
        };
    }
}
