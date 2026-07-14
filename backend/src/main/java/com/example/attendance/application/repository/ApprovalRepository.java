package com.example.attendance.application.repository;

import com.example.attendance.application.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    List<Approval> findByApplicationId(Long applicationId);

    boolean existsByApplicationId(Long applicationId);
}
