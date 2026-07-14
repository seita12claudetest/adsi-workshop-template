package com.example.attendance.employee.service;

import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.dto.EmployeeRequest;
import com.example.attendance.employee.dto.EmployeeResponse;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.organization.repository.SectionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SectionRepository sectionRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               SectionRepository sectionRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.sectionRepository = sectionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findByActiveTrue().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("社員", id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません: " + email));
    }

    @Override
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("メールアドレスが既に使用されています: " + request.email());
        }
        if (employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new IllegalArgumentException("社員コードが既に使用されています: " + request.employeeCode());
        }
        if (!sectionRepository.existsById(request.sectionId())) {
            throw new ResourceNotFoundException("課", request.sectionId());
        }

        var entity = Employee.builder()
                .employeeCode(request.employeeCode())
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role()))
                .sectionId(request.sectionId())
                .hireDate(request.hireDate())
                .active(true)
                .build();
        return EmployeeResponse.from(employeeRepository.save(entity));
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        var entity = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社員", id));
        if (!sectionRepository.existsById(request.sectionId())) {
            throw new ResourceNotFoundException("課", request.sectionId());
        }

        entity.setName(request.name());
        entity.setEmail(request.email());
        entity.setRole(Role.valueOf(request.role()));
        entity.setSectionId(request.sectionId());
        entity.setHireDate(request.hireDate());

        if (request.password() != null && !request.password().isBlank()) {
            entity.setPassword(passwordEncoder.encode(request.password()));
        }

        return EmployeeResponse.from(employeeRepository.save(entity));
    }

    @Override
    public void deactivate(Long id) {
        var entity = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社員", id));
        entity.setActive(false);
        employeeRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getManager(Long employeeId) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("社員", employeeId));
        var section = sectionRepository.findById(employee.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("課", employee.getSectionId()));

        if (section.getManagerId() == null) {
            throw new ResourceNotFoundException("この社員の課に課長が設定されていません");
        }

        return employeeRepository.findById(section.getManagerId())
                .map(EmployeeResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("課長", section.getManagerId()));
    }
}
