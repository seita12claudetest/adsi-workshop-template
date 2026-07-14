package com.example.attendance.application.service;

import com.example.attendance.application.entity.Application;
import com.example.attendance.application.entity.LeaveApplication;
import com.example.attendance.application.entity.TimeCorrectionApplication;
import com.example.attendance.application.repository.*;
import com.example.attendance.attendance.entity.DailyAttendance;
import com.example.attendance.attendance.repository.DailyAttendanceRepository;
import com.example.attendance.common.enums.*;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.leave.service.LeaveBalanceService;
import com.example.attendance.organization.entity.Section;
import com.example.attendance.organization.repository.SectionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApprovalRepository approvalRepository;
    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;
    @Mock
    private TimeCorrectionApplicationRepository timeCorrectionApplicationRepository;
    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private LeaveBalanceService leaveBalanceService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ApprovalServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApprovalServiceImpl(
                applicationRepository,
                approvalRepository,
                leaveApplicationRepository,
                timeCorrectionApplicationRepository,
                dailyAttendanceRepository,
                employeeRepository,
                sectionRepository,
                leaveBalanceService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("上長（課長）が部下の申請を承認できる")
    void approve_managerApproves_succeeds() {
        var application = Application.builder()
                .id(1L).applicantId(10L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        var applicant = Employee.builder().id(10L).sectionId(100L).role(Role.EMPLOYEE).build();
        var approver = Employee.builder().id(5L).sectionId(100L).role(Role.MANAGER).build();
        var section = Section.builder().id(100L).managerId(5L).build();

        var leaveApp = LeaveApplication.builder()
                .applicationId(1L).leaveType(LeaveType.ANNUAL)
                .startDate(LocalDate.of(2026, 7, 20)).endDate(LocalDate.of(2026, 7, 20)).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(applicant));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(approver));
        when(sectionRepository.findById(100L)).thenReturn(Optional.of(section));
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(leaveApplicationRepository.findByApplicationId(1L)).thenReturn(Optional.of(leaveApp));

        var result = service.approve(1L, 5L, "承認します");

        assertThat(result.action()).isEqualTo(ApprovalAction.APPROVED);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        verify(leaveBalanceService).consume(eq(10L), any());
    }

    @Test
    @DisplayName("ADMINが任意の申請を承認できる")
    void approve_adminApproves_succeeds() {
        var application = Application.builder()
                .id(1L).applicantId(10L).type(ApplicationType.OVERTIME)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        var approver = Employee.builder().id(99L).sectionId(200L).role(Role.ADMIN).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(employeeRepository.findById(99L)).thenReturn(Optional.of(approver));
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.approve(1L, 99L, null);

        assertThat(result.action()).isEqualTo(ApprovalAction.APPROVED);
    }

    @Test
    @DisplayName("上長が部下の申請を差戻できる（コメント付き）")
    void reject_withComment_succeeds() {
        var application = Application.builder()
                .id(1L).applicantId(10L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        var applicant = Employee.builder().id(10L).sectionId(100L).role(Role.EMPLOYEE).build();
        var approver = Employee.builder().id(5L).sectionId(100L).role(Role.MANAGER).build();
        var section = Section.builder().id(100L).managerId(5L).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(applicant));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(approver));
        when(sectionRepository.findById(100L)).thenReturn(Optional.of(section));
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.reject(1L, 5L, "日付を確認してください");

        assertThat(result.action()).isEqualTo(ApprovalAction.REJECTED);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("差戻時にコメントが空だとエラー")
    void reject_withoutComment_throwsException() {
        var application = Application.builder()
                .id(1L).applicantId(10L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        var applicant = Employee.builder().id(10L).sectionId(100L).role(Role.EMPLOYEE).build();
        var approver = Employee.builder().id(5L).sectionId(100L).role(Role.MANAGER).build();
        var section = Section.builder().id(100L).managerId(5L).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(applicant));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(approver));
        when(sectionRepository.findById(100L)).thenReturn(Optional.of(section));

        assertThatThrownBy(() -> service.reject(1L, 5L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("コメント");
    }

    @Test
    @DisplayName("承認権限がない社員は承認できない")
    void approve_noAuthority_throwsException() {
        var application = Application.builder()
                .id(1L).applicantId(10L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        var applicant = Employee.builder().id(10L).sectionId(100L).role(Role.EMPLOYEE).build();
        var approver = Employee.builder().id(20L).sectionId(200L).role(Role.EMPLOYEE).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(applicant));
        when(employeeRepository.findById(20L)).thenReturn(Optional.of(approver));
        when(sectionRepository.findById(100L)).thenReturn(Optional.of(Section.builder().id(100L).managerId(5L).build()));

        assertThatThrownBy(() -> service.approve(1L, 20L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("権限");
    }

    @Test
    @DisplayName("既に処理済みの申請は承認できない")
    void approve_alreadyProcessed_throwsException() {
        var application = Application.builder()
                .id(1L).applicantId(10L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.APPROVED).appliedAt(LocalDateTime.now()).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> service.approve(1L, 5L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("未処理承認一覧を取得できる（上長→自課の部下のみ）")
    void findPendingApprovals_manager_returnsPendingForSubordinates() {
        var approver = Employee.builder().id(5L).sectionId(100L).role(Role.MANAGER).build();
        var section = Section.builder().id(100L).managerId(5L).build();
        var subordinate = Employee.builder().id(10L).sectionId(100L).name("田中太郎").build();
        var app = Application.builder()
                .id(1L).applicantId(10L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();

        when(employeeRepository.findById(5L)).thenReturn(Optional.of(approver));
        when(sectionRepository.findById(100L)).thenReturn(Optional.of(section));
        when(employeeRepository.findBySectionId(100L)).thenReturn(List.of(subordinate, approver));
        when(applicationRepository.findPendingByApplicantIds(ApplicationStatus.PENDING, List.of(10L)))
                .thenReturn(List.of(app));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(subordinate));

        var result = service.findPendingApprovals(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).applicantId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("承認後に打刻修正の場合DailyAttendanceが更新される")
    void approve_timeCorrection_updatesDailyAttendance() {
        var application = Application.builder()
                .id(1L).applicantId(10L).type(ApplicationType.TIME_CORRECTION)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        var applicant = Employee.builder().id(10L).sectionId(100L).role(Role.EMPLOYEE).build();
        var approver = Employee.builder().id(5L).sectionId(100L).role(Role.MANAGER).build();
        var section = Section.builder().id(100L).managerId(5L).build();
        var timeCorrection = TimeCorrectionApplication.builder()
                .applicationId(1L).date(LocalDate.of(2026, 7, 13))
                .originalClockIn(LocalTime.of(9, 0)).originalClockOut(null)
                .correctedClockIn(LocalTime.of(9, 15)).correctedClockOut(LocalTime.of(18, 30)).build();
        var dailyAttendance = DailyAttendance.builder()
                .id(50L).employeeId(10L).date(LocalDate.of(2026, 7, 13))
                .clockIn(LocalTime.of(9, 0)).clockOut(null).status(DailyStatus.NORMAL).build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(applicant));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(approver));
        when(sectionRepository.findById(100L)).thenReturn(Optional.of(section));
        when(timeCorrectionApplicationRepository.findByApplicationId(1L)).thenReturn(Optional.of(timeCorrection));
        when(dailyAttendanceRepository.findByEmployeeIdAndDate(10L, LocalDate.of(2026, 7, 13)))
                .thenReturn(Optional.of(dailyAttendance));
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(applicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dailyAttendanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approve(1L, 5L, "承認します");

        assertThat(dailyAttendance.getClockIn()).isEqualTo(LocalTime.of(9, 15));
        assertThat(dailyAttendance.getClockOut()).isEqualTo(LocalTime.of(18, 30));
        verify(dailyAttendanceRepository).save(dailyAttendance);
    }
}
