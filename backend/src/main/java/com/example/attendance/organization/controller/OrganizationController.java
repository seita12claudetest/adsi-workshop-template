package com.example.attendance.organization.controller;

import com.example.attendance.organization.dto.OrganizationRequest;
import com.example.attendance.organization.dto.OrganizationResponse;
import com.example.attendance.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public List<OrganizationResponse> findAll() {
        return organizationService.findAllOrganizations();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationResponse create(@Valid @RequestBody OrganizationRequest request) {
        return organizationService.createOrganization(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OrganizationResponse update(@PathVariable Long id, @Valid @RequestBody OrganizationRequest request) {
        return organizationService.updateOrganization(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
    }
}
