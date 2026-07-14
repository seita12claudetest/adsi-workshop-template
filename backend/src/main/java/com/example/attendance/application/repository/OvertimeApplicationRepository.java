package com.example.attendance.application.repository;

import com.example.attendance.application.entity.OvertimeApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OvertimeApplicationRepository extends JpaRepository<OvertimeApplication, Long> {

    Optional<OvertimeApplication> findByApplicationId(Long applicationId);
}
