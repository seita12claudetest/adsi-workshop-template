package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.DailyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.service.AttendanceService;
import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeRepository employeeRepository;

    public AttendanceController(AttendanceService attendanceService,
                                EmployeeRepository employeeRepository) {
        this.attendanceService = attendanceService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/daily")
    public Object getDaily(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(required = false) LocalDate date,
                           @RequestParam(required = false) String yearMonth) {
        Long employeeId = getEmployeeId(userDetails);
        if (date != null) {
            return attendanceService.getDailyByDate(employeeId, date);
        }
        if (yearMonth != null) {
            return attendanceService.getDailyByMonth(employeeId, yearMonth);
        }
        return List.of();
    }

    @GetMapping("/daily/subordinates")
    public Page<DailyAttendanceResponse> getSubordinatesDaily(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String yearMonth,
            Pageable pageable) {
        Long managerId = getEmployeeIdWithManagerCheck(userDetails);
        return attendanceService.getSubordinatesDailyByMonth(managerId, yearMonth, pageable);
    }

    @GetMapping("/monthly")
    public MonthlyAttendanceResponse getMonthly(@AuthenticationPrincipal UserDetails userDetails,
                                                @RequestParam String yearMonth) {
        Long employeeId = getEmployeeId(userDetails);
        return attendanceService.getMonthly(employeeId, yearMonth);
    }

    @GetMapping("/monthly/subordinates")
    public Page<MonthlyAttendanceResponse> getSubordinatesMonthly(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String yearMonth,
            Pageable pageable) {
        Long managerId = getEmployeeIdWithManagerCheck(userDetails);
        return attendanceService.getSubordinatesMonthly(managerId, yearMonth, pageable);
    }

    private Long getEmployeeId(UserDetails userDetails) {
        return employeeRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.example.attendance.common.exception.ResourceNotFoundException("社員が見つかりません"))
                .getId();
    }

    private Long getEmployeeIdWithManagerCheck(UserDetails userDetails) {
        var employee = employeeRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.example.attendance.common.exception.ResourceNotFoundException("社員が見つかりません"));
        if (employee.getRole() != Role.MANAGER && employee.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("権限がありません");
        }
        return employee.getId();
    }
}
