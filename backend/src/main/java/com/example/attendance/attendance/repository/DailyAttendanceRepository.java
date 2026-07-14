package com.example.attendance.attendance.repository;

import com.example.attendance.attendance.entity.DailyAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyAttendanceRepository extends JpaRepository<DailyAttendance, Long> {

    Optional<DailyAttendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<DailyAttendance> findByEmployeeIdAndDateBetweenOrderByDateAsc(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    Page<DailyAttendance> findByEmployeeIdInAndDateBetweenOrderByDateAsc(
            List<Long> employeeIds, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
