package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.TimeRecordRequest;
import com.example.attendance.attendance.dto.TimeRecordResponse;
import com.example.attendance.attendance.dto.TimeRecordStatusResponse;

import java.time.LocalDate;
import java.util.List;

public interface TimeRecordService {

    TimeRecordResponse record(Long employeeId, TimeRecordRequest request);

    List<TimeRecordResponse> getByDate(Long employeeId, LocalDate date);

    TimeRecordStatusResponse getStatus(Long employeeId);
}
