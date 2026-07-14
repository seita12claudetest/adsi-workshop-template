package com.example.attendance.office.service;

import com.example.attendance.office.dto.NearestOfficeResponse;
import com.example.attendance.office.dto.OfficeRequest;
import com.example.attendance.office.dto.OfficeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OfficeService {

    Page<OfficeResponse> findAll(Pageable pageable);

    OfficeResponse findById(Long id);

    OfficeResponse create(OfficeRequest request);

    OfficeResponse update(Long id, OfficeRequest request);

    void delete(Long id);

    NearestOfficeResponse findNearest(double latitude, double longitude);
}
