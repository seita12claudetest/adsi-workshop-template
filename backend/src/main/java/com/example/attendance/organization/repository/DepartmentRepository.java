package com.example.attendance.organization.repository;

import com.example.attendance.organization.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByOrganizationId(Long organizationId);

    Optional<Department> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByOrganizationId(Long organizationId);
}
