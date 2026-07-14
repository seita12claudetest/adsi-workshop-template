package com.example.attendance.application.service;

import com.example.attendance.application.dto.*;
import com.example.attendance.application.entity.Application;
import com.example.attendance.application.entity.LeaveApplication;
import com.example.attendance.application.entity.OvertimeApplication;
import com.example.attendance.application.entity.TimeCorrectionApplication;
import com.example.attendance.application.repository.*;
import com.example.attendance.attendance.repository.DailyAttendanceRepository;
import com.example.attendance.common.enums.*;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final OvertimeApplicationRepository overtimeApplicationRepository;
    private final TimeCorrectionApplicationRepository timeCorrectionApplicationRepository;
    private final EmployeeRepository employeeRepository;
    private final DailyAttendanceRepository dailyAttendanceRepository;

    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            LeaveApplicationRepository leaveApplicationRepository,
            OvertimeApplicationRepository overtimeApplicationRepository,
            TimeCorrectionApplicationRepository timeCorrectionApplicationRepository,
            EmployeeRepository employeeRepository,
            DailyAttendanceRepository dailyAttendanceRepository) {
        this.applicationRepository = applicationRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.overtimeApplicationRepository = overtimeApplicationRepository;
        this.timeCorrectionApplicationRepository = timeCorrectionApplicationRepository;
        this.employeeRepository = employeeRepository;
        this.dailyAttendanceRepository = dailyAttendanceRepository;
    }

    @Override
    public ApplicationResponse createLeaveApplication(Long applicantId, LeaveApplicationRequest request) {
        var employee = findEmployee(applicantId);
        var leaveType = LeaveType.valueOf(request.leaveType());

        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException("終了日は開始日以降を指定してください");
        }

        var application = applicationRepository.save(Application.builder()
                .applicantId(applicantId)
                .type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING)
                .reason(request.reason())
                .build());

        var leaveApp = leaveApplicationRepository.save(LeaveApplication.builder()
                .applicationId(application.getId())
                .leaveType(leaveType)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .hours(request.hours())
                .build());

        return toResponse(application, employee, leaveApp);
    }

    @Override
    public ApplicationResponse createOvertimeApplication(Long applicantId, OvertimeApplicationRequest request) {
        var employee = findEmployee(applicantId);
        var overtimeType = OvertimeType.valueOf(request.overtimeType());

        var application = applicationRepository.save(Application.builder()
                .applicantId(applicantId)
                .type(ApplicationType.OVERTIME)
                .status(ApplicationStatus.PENDING)
                .reason(request.reason())
                .build());

        var overtimeApp = overtimeApplicationRepository.save(OvertimeApplication.builder()
                .applicationId(application.getId())
                .date(request.date())
                .expectedMinutes(request.expectedMinutes())
                .overtimeType(overtimeType)
                .build());

        return toResponse(application, employee, overtimeApp);
    }

    @Override
    public ApplicationResponse createTimeCorrectionApplication(Long applicantId, TimeCorrectionApplicationRequest request) {
        var employee = findEmployee(applicantId);

        var dailyAttendance = dailyAttendanceRepository.findByEmployeeIdAndDate(applicantId, request.date())
                .orElseThrow(() -> new BusinessException("対象日の勤怠データが見つかりません"));

        var application = applicationRepository.save(Application.builder()
                .applicantId(applicantId)
                .type(ApplicationType.TIME_CORRECTION)
                .status(ApplicationStatus.PENDING)
                .reason(request.reason())
                .build());

        var timeCorrectionApp = timeCorrectionApplicationRepository.save(TimeCorrectionApplication.builder()
                .applicationId(application.getId())
                .date(request.date())
                .originalClockIn(dailyAttendance.getClockIn())
                .originalClockOut(dailyAttendance.getClockOut())
                .correctedClockIn(request.correctedClockIn())
                .correctedClockOut(request.correctedClockOut())
                .build());

        return toResponse(application, employee, timeCorrectionApp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> findByApplicantId(Long applicantId) {
        var employee = findEmployee(applicantId);
        return applicationRepository.findByApplicantIdOrderByAppliedAtDesc(applicantId).stream()
                .map(app -> toResponse(app, employee, loadDetail(app)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        var application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("申請が見つかりません: " + id));
        var employee = findEmployee(application.getApplicantId());
        return toResponse(application, employee, loadDetail(application));
    }

    @Override
    public void cancel(Long id, Long requesterId) {
        var application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("申請が見つかりません: " + id));

        if (!application.getApplicantId().equals(requesterId)) {
            throw new BusinessException("自分の申請のみ取消できる権限があります");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("PENDING状態の申請のみ取消できます");
        }

        applicationRepository.delete(application);
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません: " + employeeId));
    }

    private Object loadDetail(Application application) {
        return switch (application.getType()) {
            case LEAVE -> leaveApplicationRepository.findByApplicationId(application.getId()).orElse(null);
            case OVERTIME -> overtimeApplicationRepository.findByApplicationId(application.getId()).orElse(null);
            case TIME_CORRECTION -> timeCorrectionApplicationRepository.findByApplicationId(application.getId()).orElse(null);
            default -> null;
        };
    }

    private ApplicationResponse toResponse(Application application, Employee employee, Object detail) {
        return new ApplicationResponse(
                application.getId(),
                application.getApplicantId(),
                employee.getName(),
                application.getType(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getReason(),
                detail
        );
    }
}
