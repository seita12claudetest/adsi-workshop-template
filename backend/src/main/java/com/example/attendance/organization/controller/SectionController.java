package com.example.attendance.organization.controller;

import com.example.attendance.organization.dto.SectionRequest;
import com.example.attendance.organization.dto.SectionResponse;
import com.example.attendance.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sections")
public class SectionController {

    private final OrganizationService organizationService;

    public SectionController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public List<SectionResponse> findByDepartmentId(@RequestParam Long departmentId) {
        return organizationService.findSectionsByDepartmentId(departmentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public SectionResponse create(@Valid @RequestBody SectionRequest request) {
        return organizationService.createSection(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SectionResponse update(@PathVariable Long id, @Valid @RequestBody SectionRequest request) {
        return organizationService.updateSection(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        organizationService.deleteSection(id);
    }
}
