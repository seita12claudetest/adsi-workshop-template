package com.example.attendance.application.service;

import com.example.attendance.application.dto.*;

import java.util.List;

public interface ApplicationService {

    ApplicationResponse createLeaveApplication(Long applicantId, LeaveApplicationRequest request);

    ApplicationResponse createOvertimeApplication(Long applicantId, OvertimeApplicationRequest request);

    ApplicationResponse createTimeCorrectionApplication(Long applicantId, TimeCorrectionApplicationRequest request);

    List<ApplicationResponse> findByApplicantId(Long applicantId);

    ApplicationResponse findById(Long id);

    void cancel(Long id, Long requesterId);
}
