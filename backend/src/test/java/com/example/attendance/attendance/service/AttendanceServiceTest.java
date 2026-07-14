package com.example.attendance.attendance.service;

import com.example.attendance.attendance.entity.DailyAttendance;
import com.example.attendance.attendance.entity.MonthlyAttendance;
import com.example.attendance.attendance.repository.DailyAttendanceRepository;
import com.example.attendance.attendance.repository.MonthlyAttendanceRepository;
import com.example.attendance.common.enums.DailyStatus;
import com.example.attendance.common.enums.MonthlyStatus;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;
    @Mock
    private MonthlyAttendanceRepository monthlyAttendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    private AttendanceServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new AttendanceServiceImpl(dailyAttendanceRepository, monthlyAttendanceRepository, employeeRepository);
    }

    @Test
    @DisplayName("日次勤怠: 指定日のデータを返す")
    void getDailyByDate_returnsAttendance() {
        var daily = DailyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .date(LocalDate.of(2026, 7, 14))
                .clockIn(LocalTime.of(9, 15))
                .clockOut(LocalTime.of(17, 30))
                .breakMinutes(60)
                .workingMinutes(435)
                .overtimeMinutes(0)
                .status(DailyStatus.NORMAL)
                .build();
        when(dailyAttendanceRepository.findByEmployeeIdAndDate(EMPLOYEE_ID, LocalDate.of(2026, 7, 14)))
                .thenReturn(Optional.of(daily));

        var response = service.getDailyByDate(EMPLOYEE_ID, LocalDate.of(2026, 7, 14));

        assertThat(response.workingMinutes()).isEqualTo(435);
        assertThat(response.overtimeMinutes()).isEqualTo(0);
        assertThat(response.clockIn()).isEqualTo(LocalTime.of(9, 15));
    }

    @Test
    @DisplayName("月次一覧: 指定月の日次勤怠リストを返す")
    void getDailyByMonth_returnsList() {
        var daily1 = DailyAttendance.builder()
                .date(LocalDate.of(2026, 7, 1)).workingMinutes(435).status(DailyStatus.NORMAL).build();
        var daily2 = DailyAttendance.builder()
                .date(LocalDate.of(2026, 7, 2)).workingMinutes(480).status(DailyStatus.NORMAL).build();

        when(dailyAttendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(
                EMPLOYEE_ID, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)))
                .thenReturn(List.of(daily1, daily2));

        var result = service.getDailyByMonth(EMPLOYEE_ID, "2026-07");

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("月次集計: データが正しく返される")
    void getMonthly_returnsMonthlySummary() {
        var monthly = MonthlyAttendance.builder()
                .employeeId(EMPLOYEE_ID)
                .yearMonth("2026-07")
                .totalWorkingMinutes(8700)
                .totalOvertimeMinutes(120)
                .workingDays(20)
                .paidLeaveDays(BigDecimal.ONE)
                .status(MonthlyStatus.OPEN)
                .build();
        when(monthlyAttendanceRepository.findByEmployeeIdAndYearMonth(EMPLOYEE_ID, "2026-07"))
                .thenReturn(Optional.of(monthly));

        var response = service.getMonthly(EMPLOYEE_ID, "2026-07");

        assertThat(response.totalWorkingMinutes()).isEqualTo(8700);
        assertThat(response.workingDays()).isEqualTo(20);
        assertThat(response.totalOvertimeMinutes()).isEqualTo(120);
    }
}
