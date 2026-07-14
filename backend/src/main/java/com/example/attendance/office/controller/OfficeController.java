package com.example.attendance.office.controller;

import com.example.attendance.office.dto.NearestOfficeResponse;
import com.example.attendance.office.dto.OfficeRequest;
import com.example.attendance.office.dto.OfficeResponse;
import com.example.attendance.office.service.OfficeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/offices")
public class OfficeController {

    private final OfficeService officeService;

    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @GetMapping
    public ResponseEntity<Page<OfficeResponse>> getOffices(Pageable pageable) {
        return ResponseEntity.ok(officeService.findAll(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OfficeResponse> createOffice(@Valid @RequestBody OfficeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(officeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfficeResponse> updateOffice(@PathVariable Long id,
                                                       @Valid @RequestBody OfficeRequest request) {
        return ResponseEntity.ok(officeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOffice(@PathVariable Long id) {
        officeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nearest")
    public ResponseEntity<NearestOfficeResponse> getNearestOffice(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        NearestOfficeResponse result = officeService.findNearest(latitude, longitude);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
