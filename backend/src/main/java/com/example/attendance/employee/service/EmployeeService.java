package com.example.attendance.employee.service;

import com.example.attendance.employee.dto.EmployeeRequest;
import com.example.attendance.employee.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponse> findAll();

    EmployeeResponse findById(Long id);

    EmployeeResponse findByEmail(String email);

    EmployeeResponse create(EmployeeRequest request);

    EmployeeResponse update(Long id, EmployeeRequest request);

    void deactivate(Long id);

    EmployeeResponse getManager(Long employeeId);
}
