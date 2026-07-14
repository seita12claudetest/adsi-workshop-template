package com.example.attendance.application.controller;

import com.example.attendance.application.dto.*;
import com.example.attendance.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createLeave(
            Authentication authentication,
            @Valid @RequestBody LeaveApplicationRequest request) {
        Long applicantId = Long.parseLong(authentication.getName());
        return applicationService.createLeaveApplication(applicantId, request);
    }

    @PostMapping("/overtime")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createOvertime(
            Authentication authentication,
            @Valid @RequestBody OvertimeApplicationRequest request) {
        Long applicantId = Long.parseLong(authentication.getName());
        return applicationService.createOvertimeApplication(applicantId, request);
    }

    @PostMapping("/time-correction")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createTimeCorrection(
            Authentication authentication,
            @Valid @RequestBody TimeCorrectionApplicationRequest request) {
        Long applicantId = Long.parseLong(authentication.getName());
        return applicationService.createTimeCorrectionApplication(applicantId, request);
    }

    @GetMapping
    public List<ApplicationResponse> findMyApplications(Authentication authentication) {
        Long applicantId = Long.parseLong(authentication.getName());
        return applicationService.findByApplicantId(applicantId);
    }

    @GetMapping("/{id}")
    public ApplicationResponse findById(@PathVariable Long id) {
        return applicationService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id, Authentication authentication) {
        Long requesterId = Long.parseLong(authentication.getName());
        applicationService.cancel(id, requesterId);
    }
}
