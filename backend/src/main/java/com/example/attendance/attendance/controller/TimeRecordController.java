package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.TimeRecordRequest;
import com.example.attendance.attendance.dto.TimeRecordResponse;
import com.example.attendance.attendance.dto.TimeRecordStatusResponse;
import com.example.attendance.attendance.service.TimeRecordService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/time-records")
public class TimeRecordController {

    private final TimeRecordService timeRecordService;
    private final com.example.attendance.employee.repository.EmployeeRepository employeeRepository;

    public TimeRecordController(TimeRecordService timeRecordService,
                                com.example.attendance.employee.repository.EmployeeRepository employeeRepository) {
        this.timeRecordService = timeRecordService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeRecordResponse record(@AuthenticationPrincipal UserDetails userDetails,
                                     @Valid @RequestBody TimeRecordRequest request) {
        Long employeeId = getEmployeeId(userDetails);
        return timeRecordService.record(employeeId, request);
    }

    @GetMapping
    public List<TimeRecordResponse> getByDate(@AuthenticationPrincipal UserDetails userDetails,
                                              @RequestParam LocalDate date) {
        Long employeeId = getEmployeeId(userDetails);
        return timeRecordService.getByDate(employeeId, date);
    }

    @GetMapping("/status")
    public TimeRecordStatusResponse getStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Long employeeId = getEmployeeId(userDetails);
        return timeRecordService.getStatus(employeeId);
    }

    private Long getEmployeeId(UserDetails userDetails) {
        return employeeRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new com.example.attendance.common.exception.ResourceNotFoundException("社員が見つかりません"))
                .getId();
    }
}
