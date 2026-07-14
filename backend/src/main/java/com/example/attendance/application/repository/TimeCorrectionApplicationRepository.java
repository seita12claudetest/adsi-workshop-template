package com.example.attendance.application.repository;

import com.example.attendance.application.entity.TimeCorrectionApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimeCorrectionApplicationRepository extends JpaRepository<TimeCorrectionApplication, Long> {

    Optional<TimeCorrectionApplication> findByApplicationId(Long applicationId);
}
