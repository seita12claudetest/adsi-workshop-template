package com.example.attendance.application.repository;

import com.example.attendance.application.entity.Application;
import com.example.attendance.common.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByApplicantIdOrderByAppliedAtDesc(Long applicantId);

    List<Application> findByApplicantIdAndStatusOrderByAppliedAtDesc(Long applicantId, ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.status = :status AND a.applicantId IN :applicantIds ORDER BY a.appliedAt DESC")
    List<Application> findPendingByApplicantIds(@Param("status") ApplicationStatus status, @Param("applicantIds") List<Long> applicantIds);
}
