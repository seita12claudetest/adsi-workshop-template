package com.example.attendance.application.service;

import com.example.attendance.application.dto.LeaveApplicationRequest;
import com.example.attendance.application.dto.OvertimeApplicationRequest;
import com.example.attendance.application.dto.TimeCorrectionApplicationRequest;
import com.example.attendance.application.entity.Application;
import com.example.attendance.application.entity.LeaveApplication;
import com.example.attendance.application.entity.OvertimeApplication;
import com.example.attendance.application.entity.TimeCorrectionApplication;
import com.example.attendance.application.repository.*;
import com.example.attendance.attendance.entity.DailyAttendance;
import com.example.attendance.attendance.repository.DailyAttendanceRepository;
import com.example.attendance.common.enums.*;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private LeaveApplicationRepository leaveApplicationRepository;
    @Mock
    private OvertimeApplicationRepository overtimeApplicationRepository;
    @Mock
    private TimeCorrectionApplicationRepository timeCorrectionApplicationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DailyAttendanceRepository dailyAttendanceRepository;

    private ApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApplicationServiceImpl(
                applicationRepository,
                leaveApplicationRepository,
                overtimeApplicationRepository,
                timeCorrectionApplicationRepository,
                employeeRepository,
                dailyAttendanceRepository
        );
    }

    @Test
    @DisplayName("休暇申請を作成できる")
    void createLeaveApplication_valid_createsApplication() {
        var request = new LeaveApplicationRequest(
                "ANNUAL", LocalDate.of(2026, 7, 20), LocalDate.of(2026, 7, 20), null, "私用のため");
        var employee = Employee.builder().id(1L).name("田中太郎").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(applicationRepository.save(any())).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(1L);
            a.setAppliedAt(LocalDateTime.now());
            return a;
        });
        when(leaveApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.createLeaveApplication(1L, request);

        assertThat(result.type()).isEqualTo(ApplicationType.LEAVE);
        assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(result.applicantName()).isEqualTo("田中太郎");
        verify(applicationRepository).save(any());
        verify(leaveApplicationRepository).save(any());
    }

    @Test
    @DisplayName("残業申請を作成できる")
    void createOvertimeApplication_valid_createsApplication() {
        var request = new OvertimeApplicationRequest(
                LocalDate.of(2026, 7, 14), 60, "PRE", "納期対応のため");
        var employee = Employee.builder().id(1L).name("田中太郎").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(applicationRepository.save(any())).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(2L);
            a.setAppliedAt(LocalDateTime.now());
            return a;
        });
        when(overtimeApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.createOvertimeApplication(1L, request);

        assertThat(result.type()).isEqualTo(ApplicationType.OVERTIME);
        assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
        verify(overtimeApplicationRepository).save(any());
    }

    @Test
    @DisplayName("打刻修正申請を作成できる")
    void createTimeCorrectionApplication_valid_createsApplication() {
        var request = new TimeCorrectionApplicationRequest(
                LocalDate.of(2026, 7, 13), LocalTime.of(9, 15), LocalTime.of(18, 30), "退勤打刻忘れ");
        var employee = Employee.builder().id(1L).name("田中太郎").build();
        var dailyAttendance = DailyAttendance.builder()
                .employeeId(1L).date(LocalDate.of(2026, 7, 13))
                .clockIn(LocalTime.of(9, 0)).clockOut(null)
                .status(DailyStatus.NORMAL).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(dailyAttendanceRepository.findByEmployeeIdAndDate(1L, LocalDate.of(2026, 7, 13)))
                .thenReturn(Optional.of(dailyAttendance));
        when(applicationRepository.save(any())).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(3L);
            a.setAppliedAt(LocalDateTime.now());
            return a;
        });
        when(timeCorrectionApplicationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.createTimeCorrectionApplication(1L, request);

        assertThat(result.type()).isEqualTo(ApplicationType.TIME_CORRECTION);
        assertThat(result.status()).isEqualTo(ApplicationStatus.PENDING);
        verify(timeCorrectionApplicationRepository).save(any());
    }

    @Test
    @DisplayName("PENDING状態の申請を取消できる")
    void cancel_pendingApplication_cancels() {
        var app = Application.builder()
                .id(1L).applicantId(1L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        service.cancel(1L, 1L);

        verify(applicationRepository).delete(app);
    }

    @Test
    @DisplayName("APPROVED状態の申請は取消できない")
    void cancel_approvedApplication_throwsException() {
        var app = Application.builder()
                .id(1L).applicantId(1L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.APPROVED).appliedAt(LocalDateTime.now()).build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> service.cancel(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("他人の申請は取消できない")
    void cancel_otherPersonsApplication_throwsException() {
        var app = Application.builder()
                .id(1L).applicantId(2L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> service.cancel(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("権限");
    }

    @Test
    @DisplayName("自分の申請一覧を取得できる")
    void findByApplicantId_returnsList() {
        var app = Application.builder()
                .id(1L).applicantId(1L).type(ApplicationType.LEAVE)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).build();
        var employee = Employee.builder().id(1L).name("田中太郎").build();
        when(applicationRepository.findByApplicantIdOrderByAppliedAtDesc(1L)).thenReturn(List.of(app));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveApplicationRepository.findByApplicationId(1L))
                .thenReturn(Optional.of(LeaveApplication.builder()
                        .applicationId(1L).leaveType(LeaveType.ANNUAL)
                        .startDate(LocalDate.of(2026, 7, 20)).endDate(LocalDate.of(2026, 7, 20)).build()));

        var result = service.findByApplicantId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(ApplicationType.LEAVE);
    }

    @Test
    @DisplayName("申請詳細を取得できる")
    void findById_existingId_returnsDetail() {
        var app = Application.builder()
                .id(1L).applicantId(1L).type(ApplicationType.OVERTIME)
                .status(ApplicationStatus.PENDING).appliedAt(LocalDateTime.now()).reason("納期対応").build();
        var employee = Employee.builder().id(1L).name("田中太郎").build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(overtimeApplicationRepository.findByApplicationId(1L))
                .thenReturn(Optional.of(OvertimeApplication.builder()
                        .applicationId(1L).date(LocalDate.of(2026, 7, 14))
                        .expectedMinutes(60).overtimeType(OvertimeType.PRE).build()));

        var result = service.findById(1L);

        assertThat(result.type()).isEqualTo(ApplicationType.OVERTIME);
        assertThat(result.reason()).isEqualTo("納期対応");
    }
}
