package com.example.attendance.attendance.repository;

import com.example.attendance.attendance.entity.MonthlyAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyAttendanceRepository extends JpaRepository<MonthlyAttendance, Long> {

    Optional<MonthlyAttendance> findByEmployeeIdAndYearMonth(Long employeeId, String yearMonth);

    Page<MonthlyAttendance> findByEmployeeIdInAndYearMonth(
            List<Long> employeeIds, String yearMonth, Pageable pageable);
}
