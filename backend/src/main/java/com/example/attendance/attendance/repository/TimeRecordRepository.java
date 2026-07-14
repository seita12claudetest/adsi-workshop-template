package com.example.attendance.attendance.repository;

import com.example.attendance.attendance.entity.TimeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    List<TimeRecord> findByEmployeeIdAndDateOrderByRecordedAtAsc(Long employeeId, LocalDate date);
}
