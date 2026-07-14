package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.DailyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    DailyAttendanceResponse getDailyByDate(Long employeeId, LocalDate date);

    List<DailyAttendanceResponse> getDailyByMonth(Long employeeId, String yearMonth);

    Page<DailyAttendanceResponse> getSubordinatesDailyByMonth(Long managerId, String yearMonth, Pageable pageable);

    MonthlyAttendanceResponse getMonthly(Long employeeId, String yearMonth);

    Page<MonthlyAttendanceResponse> getSubordinatesMonthly(Long managerId, String yearMonth, Pageable pageable);
}
