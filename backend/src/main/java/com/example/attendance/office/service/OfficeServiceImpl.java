package com.example.attendance.office.service;

import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.office.dto.NearestOfficeResponse;
import com.example.attendance.office.dto.OfficeRequest;
import com.example.attendance.office.dto.OfficeResponse;
import com.example.attendance.office.entity.Office;
import com.example.attendance.office.repository.OfficeRepository;
import com.example.attendance.office.vo.Distance;
import com.example.attendance.office.vo.GeoLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;

    public OfficeServiceImpl(OfficeRepository officeRepository) {
        this.officeRepository = officeRepository;
    }

    @Override
    public Page<OfficeResponse> findAll(Pageable pageable) {
        return officeRepository.findAll(pageable).map(OfficeResponse::from);
    }

    @Override
    public OfficeResponse findById(Long id) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("拠点", id));
        return OfficeResponse.from(office);
    }

    @Override
    @Transactional
    public OfficeResponse create(OfficeRequest request) {
        Office office = Office.builder()
                .name(request.name())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .radiusMeters(request.radiusMeters())
                .build();
        return OfficeResponse.from(officeRepository.save(office));
    }

    @Override
    @Transactional
    public OfficeResponse update(Long id, OfficeRequest request) {
        Office office = officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("拠点", id));
        office.setName(request.name());
        office.setAddress(request.address());
        office.setLatitude(request.latitude());
        office.setLongitude(request.longitude());
        office.setRadiusMeters(request.radiusMeters());
        return OfficeResponse.from(officeRepository.save(office));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!officeRepository.existsById(id)) {
            throw new ResourceNotFoundException("拠点", id);
        }
        officeRepository.deleteById(id);
    }

    @Override
    public NearestOfficeResponse findNearest(double latitude, double longitude) {
        List<Office> offices = officeRepository.findAll();
        if (offices.isEmpty()) {
            return null;
        }

        GeoLocation currentLocation = GeoLocation.of(latitude, longitude);

        Office nearest = null;
        Distance shortestDistance = null;

        for (Office office : offices) {
            Distance distance = currentLocation.distanceTo(office.toGeoLocation());
            if (shortestDistance == null || distance.meters() < shortestDistance.meters()) {
                shortestDistance = distance;
                nearest = office;
            }
        }

        boolean withinArea = shortestDistance.isWithin(nearest.getRadiusMeters());

        return new NearestOfficeResponse(
                OfficeResponse.from(nearest),
                shortestDistance.meters(),
                shortestDistance.toFormattedString(),
                withinArea
        );
    }
}
