package com.example.attendance.organization.service;

import com.example.attendance.organization.dto.*;

import java.util.List;

public interface OrganizationService {

    List<OrganizationResponse> findAllOrganizations();

    OrganizationResponse createOrganization(OrganizationRequest request);

    OrganizationResponse updateOrganization(Long id, OrganizationRequest request);

    void deleteOrganization(Long id);

    List<DepartmentResponse> findDepartmentsByOrganizationId(Long organizationId);

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);

    void deleteDepartment(Long id);

    List<SectionResponse> findSectionsByDepartmentId(Long departmentId);

    SectionResponse createSection(SectionRequest request);

    SectionResponse updateSection(Long id, SectionRequest request);

    void deleteSection(Long id);
}
