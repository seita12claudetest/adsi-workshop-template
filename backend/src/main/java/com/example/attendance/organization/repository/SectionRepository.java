package com.example.attendance.organization.repository;

import com.example.attendance.organization.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByDepartmentId(Long departmentId);

    Optional<Section> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByDepartmentId(Long departmentId);
}
