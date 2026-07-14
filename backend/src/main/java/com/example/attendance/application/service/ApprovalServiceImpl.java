package com.example.attendance.application.service;

import com.example.attendance.application.dto.ApplicationResponse;
import com.example.attendance.application.dto.ApprovalResponse;
import com.example.attendance.application.entity.Approval;
import com.example.attendance.application.entity.Application;
import com.example.attendance.application.entity.LeaveApplication;
import com.example.attendance.application.repository.*;
import com.example.attendance.attendance.repository.DailyAttendanceRepository;
import com.example.attendance.common.enums.*;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.leave.service.LeaveBalanceService;
import com.example.attendance.notification.event.NotificationEvent;
import com.example.attendance.organization.entity.Section;
import com.example.attendance.organization.repository.SectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ApprovalServiceImpl implements ApprovalService {

    private final ApplicationRepository applicationRepository;
    private final ApprovalRepository approvalRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final TimeCorrectionApplicationRepository timeCorrectionApplicationRepository;
    private final DailyAttendanceRepository dailyAttendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final SectionRepository sectionRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final ApplicationEventPublisher eventPublisher;

    public ApprovalServiceImpl(
            ApplicationRepository applicationRepository,
            ApprovalRepository approvalRepository,
            LeaveApplicationRepository leaveApplicationRepository,
            TimeCorrectionApplicationRepository timeCorrectionApplicationRepository,
            DailyAttendanceRepository dailyAttendanceRepository,
            EmployeeRepository employeeRepository,
            SectionRepository sectionRepository,
            LeaveBalanceService leaveBalanceService,
            ApplicationEventPublisher eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.approvalRepository = approvalRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.timeCorrectionApplicationRepository = timeCorrectionApplicationRepository;
        this.dailyAttendanceRepository = dailyAttendanceRepository;
        this.employeeRepository = employeeRepository;
        this.sectionRepository = sectionRepository;
        this.leaveBalanceService = leaveBalanceService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ApprovalResponse approve(Long applicationId, Long approverId, String comment) {
        var application = findPendingApplication(applicationId);
        validateApprovalAuthority(application, approverId);

        application.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(application);

        var approval = approvalRepository.save(Approval.builder()
                .applicationId(applicationId)
                .approverId(approverId)
                .action(ApprovalAction.APPROVED)
                .comment(comment)
                .build());

        applyPostApprovalEffects(application);

        eventPublisher.publishEvent(NotificationEvent.approvalResult(
                application.getApplicantId(), applicationId,
                application.getType().name(), true));

        return new ApprovalResponse(applicationId, ApprovalAction.APPROVED, approval.getDecidedAt());
    }

    @Override
    public ApprovalResponse reject(Long applicationId, Long approverId, String comment) {
        var application = findPendingApplication(applicationId);
        validateApprovalAuthority(application, approverId);

        if (comment == null || comment.isBlank()) {
            throw new BusinessException("差戻時はコメントが必須です");
        }

        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);

        var approval = approvalRepository.save(Approval.builder()
                .applicationId(applicationId)
                .approverId(approverId)
                .action(ApprovalAction.REJECTED)
                .comment(comment)
                .build());

        eventPublisher.publishEvent(NotificationEvent.approvalResult(
                application.getApplicantId(), applicationId,
                application.getType().name(), false));

        return new ApprovalResponse(applicationId, ApprovalAction.REJECTED, approval.getDecidedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> findPendingApprovals(Long approverId) {
        var approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません: " + approverId));

        List<Long> subordinateIds;
        if (approver.getRole() == Role.ADMIN) {
            subordinateIds = applicationRepository
                    .findByApplicantIdAndStatusOrderByAppliedAtDesc(null, ApplicationStatus.PENDING)
                    .stream().map(Application::getApplicantId).toList();
            var pendingApplications = applicationRepository
                    .findPendingByApplicantIds(ApplicationStatus.PENDING, List.of());
            // For ADMIN, get all pending applications
            return applicationRepository.findByApplicantIdAndStatusOrderByAppliedAtDesc(null, ApplicationStatus.PENDING)
                    .stream().map(app -> toResponse(app)).toList();
        }

        var section = sectionRepository.findById(approver.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("課が見つかりません"));

        subordinateIds = employeeRepository.findBySectionId(section.getId()).stream()
                .map(Employee::getId)
                .filter(id -> !id.equals(approverId))
                .toList();

        if (subordinateIds.isEmpty()) {
            return List.of();
        }

        return applicationRepository.findPendingByApplicantIds(ApplicationStatus.PENDING, subordinateIds)
                .stream().map(app -> toResponse(app)).toList();
    }

    private Application findPendingApplication(Long applicationId) {
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("申請が見つかりません: " + applicationId));
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException("PENDING状態の申請のみ処理できます");
        }
        return application;
    }

    private void validateApprovalAuthority(Application application, Long approverId) {
        var approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("承認者が見つかりません: " + approverId));

        if (approver.getRole() == Role.ADMIN) {
            return;
        }

        var applicant = employeeRepository.findById(application.getApplicantId())
                .orElseThrow(() -> new ResourceNotFoundException("申請者が見つかりません"));

        var section = sectionRepository.findById(applicant.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("課が見つかりません"));

        if (!approverId.equals(section.getManagerId())) {
            throw new BusinessException("この申請を承認する権限がありません");
        }
    }

    private void applyPostApprovalEffects(Application application) {
        if (application.getType() == ApplicationType.LEAVE) {
            applyLeaveConsumption(application);
        } else if (application.getType() == ApplicationType.TIME_CORRECTION) {
            applyTimeCorrection(application);
        }
    }

    private void applyLeaveConsumption(Application application) {
        var leaveApp = leaveApplicationRepository.findByApplicationId(application.getId())
                .orElseThrow(() -> new ResourceNotFoundException("休暇申請明細が見つかりません"));

        if (leaveApp.getLeaveType() != LeaveType.ANNUAL
                && leaveApp.getLeaveType() != LeaveType.HALF_AM
                && leaveApp.getLeaveType() != LeaveType.HALF_PM) {
            return;
        }

        BigDecimal days;
        if (leaveApp.getLeaveType() == LeaveType.HALF_AM || leaveApp.getLeaveType() == LeaveType.HALF_PM) {
            days = BigDecimal.valueOf(0.5);
        } else {
            days = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(leaveApp.getStartDate(), leaveApp.getEndDate()) + 1
            );
        }

        leaveBalanceService.consume(application.getApplicantId(), days);
    }

    private void applyTimeCorrection(Application application) {
        var timeCorrection = timeCorrectionApplicationRepository.findByApplicationId(application.getId())
                .orElseThrow(() -> new ResourceNotFoundException("打刻修正明細が見つかりません"));

        var dailyAttendance = dailyAttendanceRepository
                .findByEmployeeIdAndDate(application.getApplicantId(), timeCorrection.getDate())
                .orElseThrow(() -> new ResourceNotFoundException("日次勤怠が見つかりません"));

        if (timeCorrection.getCorrectedClockIn() != null) {
            dailyAttendance.setClockIn(timeCorrection.getCorrectedClockIn());
        }
        if (timeCorrection.getCorrectedClockOut() != null) {
            dailyAttendance.setClockOut(timeCorrection.getCorrectedClockOut());
        }

        dailyAttendanceRepository.save(dailyAttendance);
    }

    private ApplicationResponse toResponse(Application application) {
        var employee = employeeRepository.findById(application.getApplicantId()).orElse(null);
        String name = employee != null ? employee.getName() : "不明";
        return new ApplicationResponse(
                application.getId(),
                application.getApplicantId(),
                name,
                application.getType(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getReason(),
                null
        );
    }
}
