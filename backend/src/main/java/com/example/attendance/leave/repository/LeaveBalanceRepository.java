package com.example.attendance.leave.repository;

import com.example.attendance.leave.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.employeeId = :employeeId " +
           "AND lb.expiryDate >= :today AND lb.remainingDays > 0 " +
           "ORDER BY lb.grantDate ASC")
    List<LeaveBalance> findActiveBalances(
        @Param("employeeId") Long employeeId,
        @Param("today") LocalDate today
    );

    List<LeaveBalance> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeIdAndFiscalYear(Long employeeId, Integer fiscalYear);

    @Query("SELECT lb FROM LeaveBalance lb WHERE lb.expiryDate < :today AND lb.remainingDays > 0")
    List<LeaveBalance> findExpiredWithRemaining(@Param("today") LocalDate today);
}
