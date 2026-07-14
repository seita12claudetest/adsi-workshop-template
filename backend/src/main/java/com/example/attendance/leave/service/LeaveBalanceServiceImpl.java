package com.example.attendance.leave.service;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.leave.dto.ConsumeResult;
import com.example.attendance.leave.dto.GrantResultResponse;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveBalanceSummaryResponse;
import com.example.attendance.leave.entity.LeaveBalance;
import com.example.attendance.leave.repository.LeaveBalanceRepository;
import com.example.attendance.leave.vo.FiscalYear;
import com.example.attendance.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private static final BigDecimal LEAVE_ALERT_THRESHOLD = new BigDecimal("3");

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveGrantCalculator leaveGrantCalculator;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    public LeaveBalanceServiceImpl(
            LeaveBalanceRepository leaveBalanceRepository,
            EmployeeRepository employeeRepository,
            LeaveGrantCalculator leaveGrantCalculator,
            Clock clock,
            ApplicationEventPublisher eventPublisher) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
        this.leaveGrantCalculator = leaveGrantCalculator;
        this.clock = clock;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public LeaveBalanceSummaryResponse getBalance(Long employeeId) {
        LocalDate today = LocalDate.now(clock);
        List<LeaveBalance> actives = leaveBalanceRepository.findActiveBalances(employeeId, today);

        BigDecimal total = actives.stream()
            .map(LeaveBalance::getRemainingDays)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<LeaveBalanceResponse> responses = actives.stream()
            .map(this::toResponse)
            .toList();

        return new LeaveBalanceSummaryResponse(employeeId, total, responses);
    }

    @Override
    public boolean hasEnoughBalance(Long employeeId, BigDecimal days) {
        LocalDate today = LocalDate.now(clock);
        List<LeaveBalance> actives = leaveBalanceRepository.findActiveBalances(employeeId, today);

        BigDecimal total = actives.stream()
            .map(LeaveBalance::getRemainingDays)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.compareTo(days) >= 0;
    }

    @Override
    @Transactional
    public ConsumeResult consume(Long employeeId, BigDecimal days) {
        LocalDate today = LocalDate.now(clock);
        List<LeaveBalance> actives = leaveBalanceRepository.findActiveBalances(employeeId, today);

        BigDecimal total = actives.stream()
            .map(LeaveBalance::getRemainingDays)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.compareTo(days) < 0) {
            throw new BusinessException("有給残日数が不足しています（残: " + total + "日, 申請: " + days + "日）");
        }

        BigDecimal remaining = days;
        List<ConsumeResult.ConsumeDetail> details = new ArrayList<>();

        for (LeaveBalance balance : actives) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal consumed = balance.consume(remaining);
            if (consumed.compareTo(BigDecimal.ZERO) > 0) {
                details.add(new ConsumeResult.ConsumeDetail(balance.getFiscalYear(), consumed));
                remaining = remaining.subtract(consumed);
            }
        }

        BigDecimal newTotal = total.subtract(days.subtract(remaining));
        if (newTotal.compareTo(LEAVE_ALERT_THRESHOLD) <= 0) {
            eventPublisher.publishEvent(NotificationEvent.leaveBalanceAlert(employeeId, newTotal));
        }

        return new ConsumeResult(days.subtract(remaining), details);
    }

    @Override
    @Transactional
    public GrantResultResponse grantAnnualLeave(int fiscalYear) {
        FiscalYear fy = FiscalYear.of(fiscalYear);
        LocalDate grantDate = fy.grantDate();
        LocalDate expiryDate = fy.expiryDate();

        var employees = employeeRepository.findByActiveTrue();

        int granted = 0;
        int skipped = 0;
        int alreadyGranted = 0;

        for (var employee : employees) {
            if (leaveBalanceRepository.existsByEmployeeIdAndFiscalYear(employee.getId(), fiscalYear)) {
                alreadyGranted++;
                continue;
            }

            BigDecimal days = leaveGrantCalculator.calculateForEmployee(employee.getHireDate(), grantDate);
            if (days.compareTo(BigDecimal.ZERO) <= 0) {
                skipped++;
                continue;
            }

            LeaveBalance balance = LeaveBalance.builder()
                .employeeId(employee.getId())
                .fiscalYear(fiscalYear)
                .grantedDays(days)
                .usedDays(BigDecimal.ZERO)
                .remainingDays(days)
                .grantDate(grantDate)
                .expiryDate(expiryDate)
                .build();

            leaveBalanceRepository.save(balance);
            granted++;
        }

        return new GrantResultResponse(granted, skipped, alreadyGranted);
    }

    @Override
    @Transactional
    public int expireOutdatedBalances() {
        LocalDate today = LocalDate.now(clock);
        List<LeaveBalance> expired = leaveBalanceRepository.findExpiredWithRemaining(today);

        for (LeaveBalance balance : expired) {
            balance.setRemainingDays(BigDecimal.ZERO);
        }

        return expired.size();
    }

    private LeaveBalanceResponse toResponse(LeaveBalance entity) {
        return new LeaveBalanceResponse(
            entity.getId(),
            entity.getFiscalYear(),
            entity.getGrantedDays(),
            entity.getUsedDays(),
            entity.getRemainingDays(),
            entity.getGrantDate(),
            entity.getExpiryDate()
        );
    }
}
