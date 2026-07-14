package com.example.attendance.organization.service;

import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.organization.dto.*;
import com.example.attendance.organization.entity.Department;
import com.example.attendance.organization.entity.Organization;
import com.example.attendance.organization.entity.Section;
import com.example.attendance.organization.repository.DepartmentRepository;
import com.example.attendance.organization.repository.OrganizationRepository;
import com.example.attendance.organization.repository.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final SectionRepository sectionRepository;
    private final EmployeeRepository employeeRepository;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   DepartmentRepository departmentRepository,
                                   SectionRepository sectionRepository,
                                   EmployeeRepository employeeRepository) {
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.sectionRepository = sectionRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponse> findAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(OrganizationResponse::from)
                .toList();
    }

    @Override
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        if (organizationRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("本部コードが既に使用されています: " + request.code());
        }
        var entity = Organization.builder()
                .name(request.name())
                .code(request.code())
                .build();
        return OrganizationResponse.from(organizationRepository.save(entity));
    }

    @Override
    public OrganizationResponse updateOrganization(Long id, OrganizationRequest request) {
        var entity = organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("本部が見つかりません: " + id));
        entity.setName(request.name());
        entity.setCode(request.code());
        return OrganizationResponse.from(organizationRepository.save(entity));
    }

    @Override
    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new ResourceNotFoundException("本部が見つかりません: " + id);
        }
        if (departmentRepository.existsByOrganizationId(id)) {
            throw new IllegalStateException("部が存在する本部は削除できません");
        }
        organizationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> findDepartmentsByOrganizationId(Long organizationId) {
        return departmentRepository.findByOrganizationId(organizationId).stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (!organizationRepository.existsById(request.organizationId())) {
            throw new ResourceNotFoundException("本部が見つかりません: " + request.organizationId());
        }
        if (departmentRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("部コードが既に使用されています: " + request.code());
        }
        var entity = Department.builder()
                .organizationId(request.organizationId())
                .name(request.name())
                .code(request.code())
                .build();
        return DepartmentResponse.from(departmentRepository.save(entity));
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        var entity = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("部が見つかりません: " + id));
        entity.setName(request.name());
        entity.setCode(request.code());
        return DepartmentResponse.from(departmentRepository.save(entity));
    }

    @Override
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("部が見つかりません: " + id);
        }
        if (sectionRepository.existsByDepartmentId(id)) {
            throw new IllegalStateException("課が存在する部は削除できません");
        }
        departmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> findSectionsByDepartmentId(Long departmentId) {
        return sectionRepository.findByDepartmentId(departmentId).stream()
                .map(SectionResponse::from)
                .toList();
    }

    @Override
    public SectionResponse createSection(SectionRequest request) {
        if (!departmentRepository.existsById(request.departmentId())) {
            throw new ResourceNotFoundException("部が見つかりません: " + request.departmentId());
        }
        if (sectionRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("課コードが既に使用されています: " + request.code());
        }
        if (request.managerId() != null && !employeeRepository.existsById(request.managerId())) {
            throw new ResourceNotFoundException("課長に指定された社員が見つかりません: " + request.managerId());
        }
        var entity = Section.builder()
                .departmentId(request.departmentId())
                .name(request.name())
                .code(request.code())
                .managerId(request.managerId())
                .build();
        return SectionResponse.from(sectionRepository.save(entity));
    }

    @Override
    public SectionResponse updateSection(Long id, SectionRequest request) {
        var entity = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("課が見つかりません: " + id));
        if (request.managerId() != null && !employeeRepository.existsById(request.managerId())) {
            throw new ResourceNotFoundException("課長に指定された社員が見つかりません: " + request.managerId());
        }
        entity.setName(request.name());
        entity.setCode(request.code());
        entity.setManagerId(request.managerId());
        return SectionResponse.from(sectionRepository.save(entity));
    }

    @Override
    public void deleteSection(Long id) {
        if (!sectionRepository.existsById(id)) {
            throw new ResourceNotFoundException("課が見つかりません: " + id);
        }
        if (employeeRepository.existsBySectionId(id)) {
            throw new IllegalStateException("社員が所属する課は削除できません");
        }
        sectionRepository.deleteById(id);
    }
}
