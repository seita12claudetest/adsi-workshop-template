package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.TimeRecordRequest;
import com.example.attendance.attendance.entity.DailyAttendance;
import com.example.attendance.attendance.entity.TimeRecord;
import com.example.attendance.attendance.repository.DailyAttendanceRepository;
import com.example.attendance.attendance.repository.MonthlyAttendanceRepository;
import com.example.attendance.attendance.repository.TimeRecordRepository;
import com.example.attendance.attendance.vo.ClockState;
import com.example.attendance.common.enums.TimeRecordType;
import com.example.attendance.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeRecordServiceTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;
    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;
    @Mock
    private MonthlyAttendanceRepository monthlyAttendanceRepository;

    private TimeRecordServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 7, 14);
    private static final Instant FIXED_INSTANT = LocalDateTime.of(TODAY, LocalTime.of(17, 30))
            .atZone(ZoneId.systemDefault()).toInstant();
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        service = new TimeRecordServiceImpl(
                timeRecordRepository, dailyAttendanceRepository, monthlyAttendanceRepository, FIXED_CLOCK);
    }

    @Test
    @DisplayName("出勤打刻: NOT_CLOCKED_IN → WORKING に遷移し、DailyAttendance が生成される")
    void clockIn_createsTimeRecordAndDailyAttendance() {
        when(timeRecordRepository.findByEmployeeIdAndDateOrderByRecordedAtAsc(EMPLOYEE_ID, TODAY))
                .thenReturn(Collections.emptyList());
        when(dailyAttendanceRepository.findByEmployeeIdAndDate(EMPLOYEE_ID, TODAY))
                .thenReturn(Optional.empty());
        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(i -> {
                    TimeRecord r = i.getArgument(0);
                    r.setId(1L);
                    return r;
                });
        when(dailyAttendanceRepository.save(any(DailyAttendance.class)))
                .thenAnswer(i -> i.getArgument(0));

        var request = new TimeRecordRequest(TimeRecordType.CLOCK_IN, 35.6812, 139.7671);
        var response = service.record(EMPLOYEE_ID, request);

        assertThat(response.type()).isEqualTo(TimeRecordType.CLOCK_IN);
        assertThat(response.id()).isEqualTo(1L);

        var captor = ArgumentCaptor.forClass(DailyAttendance.class);
        verify(dailyAttendanceRepository).save(captor.capture());
        assertThat(captor.getValue().getClockIn()).isNotNull();
    }

    @Test
    @DisplayName("退勤打刻: WORKING → CLOCKED_OUT に遷移し、勤怠が計算される")
    void clockOut_calculatesAttendance() {
        var clockInRecord = TimeRecord.builder()
                .type(TimeRecordType.CLOCK_IN)
                .recordedAt(LocalDateTime.of(TODAY, java.time.LocalTime.of(9, 0)))
                .date(TODAY)
                .build();
        when(timeRecordRepository.findByEmployeeIdAndDateOrderByRecordedAtAsc(EMPLOYEE_ID, TODAY))
                .thenReturn(List.of(clockInRecord));

        var existingDaily = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .date(TODAY)
                .clockIn(java.time.LocalTime.of(9, 0))
                .build();
        when(dailyAttendanceRepository.findByEmployeeIdAndDate(EMPLOYEE_ID, TODAY))
                .thenReturn(Optional.of(existingDaily));
        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(i -> {
                    TimeRecord r = i.getArgument(0);
                    r.setId(2L);
                    return r;
                });
        when(dailyAttendanceRepository.save(any(DailyAttendance.class)))
                .thenAnswer(i -> i.getArgument(0));

        var request = new TimeRecordRequest(TimeRecordType.CLOCK_OUT, null, null);
        var response = service.record(EMPLOYEE_ID, request);

        assertThat(response.type()).isEqualTo(TimeRecordType.CLOCK_OUT);

        var captor = ArgumentCaptor.forClass(DailyAttendance.class);
        verify(dailyAttendanceRepository).save(captor.capture());
        var daily = captor.getValue();
        assertThat(daily.getClockOut()).isNotNull();
        assertThat(daily.getWorkingMinutes()).isNotNull();
        assertThat(daily.getWorkingMinutes()).isGreaterThan(0);
    }

    @Test
    @DisplayName("重複出勤: WORKING 状態で CLOCK_IN → BusinessException")
    void clockIn_alreadyWorking_throwsException() {
        var clockInRecord = TimeRecord.builder()
                .type(TimeRecordType.CLOCK_IN)
                .recordedAt(LocalDateTime.of(TODAY, java.time.LocalTime.of(9, 0)))
                .date(TODAY)
                .build();
        when(timeRecordRepository.findByEmployeeIdAndDateOrderByRecordedAtAsc(EMPLOYEE_ID, TODAY))
                .thenReturn(List.of(clockInRecord));

        var request = new TimeRecordRequest(TimeRecordType.CLOCK_IN, null, null);

        assertThatThrownBy(() -> service.record(EMPLOYEE_ID, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("未出勤で退勤: NOT_CLOCKED_IN → CLOCK_OUT → BusinessException")
    void clockOut_notClockedIn_throwsException() {
        when(timeRecordRepository.findByEmployeeIdAndDateOrderByRecordedAtAsc(EMPLOYEE_ID, TODAY))
                .thenReturn(Collections.emptyList());

        var request = new TimeRecordRequest(TimeRecordType.CLOCK_OUT, null, null);

        assertThatThrownBy(() -> service.record(EMPLOYEE_ID, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("休憩開始: WORKING → ON_BREAK に遷移")
    void breakStart_fromWorking_succeeds() {
        var clockInRecord = TimeRecord.builder()
                .type(TimeRecordType.CLOCK_IN)
                .recordedAt(LocalDateTime.of(TODAY, java.time.LocalTime.of(9, 0)))
                .date(TODAY)
                .build();
        when(timeRecordRepository.findByEmployeeIdAndDateOrderByRecordedAtAsc(EMPLOYEE_ID, TODAY))
                .thenReturn(List.of(clockInRecord));
        when(timeRecordRepository.save(any(TimeRecord.class)))
                .thenAnswer(i -> {
                    TimeRecord r = i.getArgument(0);
                    r.setId(3L);
                    return r;
                });

        var request = new TimeRecordRequest(TimeRecordType.BREAK_START, null, null);
        var response = service.record(EMPLOYEE_ID, request);

        assertThat(response.type()).isEqualTo(TimeRecordType.BREAK_START);
    }

    @Test
    @DisplayName("退勤済みで休憩開始: CLOCKED_OUT → BREAK_START → BusinessException")
    void breakStart_afterClockOut_throwsException() {
        var records = List.of(
                TimeRecord.builder()
                        .type(TimeRecordType.CLOCK_IN)
                        .recordedAt(LocalDateTime.of(TODAY, java.time.LocalTime.of(9, 0)))
                        .date(TODAY).build(),
                TimeRecord.builder()
                        .type(TimeRecordType.CLOCK_OUT)
                        .recordedAt(LocalDateTime.of(TODAY, java.time.LocalTime.of(17, 0)))
                        .date(TODAY).build()
        );
        when(timeRecordRepository.findByEmployeeIdAndDateOrderByRecordedAtAsc(EMPLOYEE_ID, TODAY))
                .thenReturn(records);

        var request = new TimeRecordRequest(TimeRecordType.BREAK_START, null, null);

        assertThatThrownBy(() -> service.record(EMPLOYEE_ID, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("打刻状態取得: 出勤済み → WORKING, 次アクション BREAK_START/CLOCK_OUT")
    void getStatus_working_returnsCorrectState() {
        var clockInRecord = TimeRecord.builder()
                .type(TimeRecordType.CLOCK_IN)
                .recordedAt(LocalDateTime.of(TODAY, java.time.LocalTime.of(9, 0)))
                .date(TODAY)
                .build();
        when(timeRecordRepository.findByEmployeeIdAndDateOrderByRecordedAtAsc(eq(EMPLOYEE_ID), any(LocalDate.class)))
                .thenReturn(List.of(clockInRecord));

        var status = service.getStatus(EMPLOYEE_ID);

        assertThat(status.currentState()).isEqualTo(ClockState.WORKING);
        assertThat(status.nextActions()).containsExactlyInAnyOrder(
                TimeRecordType.BREAK_START, TimeRecordType.CLOCK_OUT);
        assertThat(status.clockInAt()).isEqualTo(java.time.LocalTime.of(9, 0));
    }
}
