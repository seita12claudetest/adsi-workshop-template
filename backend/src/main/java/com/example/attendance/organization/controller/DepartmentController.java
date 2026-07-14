package com.example.attendance.organization.controller;

import com.example.attendance.organization.dto.DepartmentRequest;
import com.example.attendance.organization.dto.DepartmentResponse;
import com.example.attendance.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final OrganizationService organizationService;

    public DepartmentController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public List<DepartmentResponse> findByOrganizationId(@RequestParam Long organizationId) {
        return organizationService.findDepartmentsByOrganizationId(organizationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentResponse create(@Valid @RequestBody DepartmentRequest request) {
        return organizationService.createDepartment(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return organizationService.updateDepartment(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        organizationService.deleteDepartment(id);
    }
}
