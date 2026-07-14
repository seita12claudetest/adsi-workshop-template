package com.example.attendance.application.repository;

import com.example.attendance.application.entity.LeaveApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

    Optional<LeaveApplication> findByApplicationId(Long applicationId);
}
