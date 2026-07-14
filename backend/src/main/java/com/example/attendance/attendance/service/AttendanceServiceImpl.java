package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.DailyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.entity.DailyAttendance;
import com.example.attendance.attendance.entity.MonthlyAttendance;
import com.example.attendance.attendance.repository.DailyAttendanceRepository;
import com.example.attendance.attendance.repository.MonthlyAttendanceRepository;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttendanceServiceImpl implements AttendanceService {

    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final MonthlyAttendanceRepository monthlyAttendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceServiceImpl(DailyAttendanceRepository dailyAttendanceRepository,
                                 MonthlyAttendanceRepository monthlyAttendanceRepository,
                                 EmployeeRepository employeeRepository) {
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.monthlyAttendanceRepository = monthlyAttendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public DailyAttendanceResponse getDailyByDate(Long employeeId, LocalDate date) {
        DailyAttendance daily = dailyAttendanceRepository.findByEmployeeIdAndDate(employeeId, date)
                .orElseThrow(() -> new ResourceNotFoundException("指定日の勤怠データが見つかりません"));
        return toResponse(daily);
    }

    @Override
    public List<DailyAttendanceResponse> getDailyByMonth(Long employeeId, String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return dailyAttendanceRepository.findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public Page<DailyAttendanceResponse> getSubordinatesDailyByMonth(Long managerId, String yearMonth, Pageable pageable) {
        List<Long> subordinateIds = getSubordinateIds(managerId);
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return dailyAttendanceRepository
                .findByEmployeeIdInAndDateBetweenOrderByDateAsc(subordinateIds, start, end, pageable)
                .map(this::toResponse);
    }

    @Override
    public MonthlyAttendanceResponse getMonthly(Long employeeId, String yearMonth) {
        MonthlyAttendance monthly = monthlyAttendanceRepository
                .findByEmployeeIdAndYearMonth(employeeId, yearMonth)
                .orElseThrow(() -> new ResourceNotFoundException("指定月の勤怠データが見つかりません"));
        return toMonthlyResponse(monthly);
    }

    @Override
    public Page<MonthlyAttendanceResponse> getSubordinatesMonthly(Long managerId, String yearMonth, Pageable pageable) {
        List<Long> subordinateIds = getSubordinateIds(managerId);
        return monthlyAttendanceRepository
                .findByEmployeeIdInAndYearMonth(subordinateIds, yearMonth, pageable)
                .map(this::toMonthlyResponse);
    }

    private List<Long> getSubordinateIds(Long managerId) {
        Employee manager = employeeRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));
        return employeeRepository.findBySectionId(manager.getSectionId())
                .stream()
                .filter(e -> !e.getId().equals(managerId))
                .map(Employee::getId)
                .toList();
    }

    private DailyAttendanceResponse toResponse(DailyAttendance daily) {
        return new DailyAttendanceResponse(
                daily.getDate(),
                daily.getClockIn(),
                daily.getClockOut(),
                daily.getBreakStart(),
                daily.getBreakEnd(),
                daily.getWorkingMinutes(),
                daily.getOvertimeMinutes(),
                daily.getBreakMinutes(),
                daily.getStatus()
        );
    }

    private MonthlyAttendanceResponse toMonthlyResponse(MonthlyAttendance monthly) {
        return new MonthlyAttendanceResponse(
                monthly.getYearMonth(),
                monthly.getTotalWorkingMinutes(),
                monthly.getTotalOvertimeMinutes(),
                monthly.getWorkingDays(),
                monthly.getPaidLeaveDays(),
                monthly.getStatus()
        );
    }
}
