package com.example.attendance.leave.service;

import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.leave.dto.ConsumeResult;
import com.example.attendance.leave.entity.LeaveBalance;
import com.example.attendance.leave.repository.LeaveBalanceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveBalanceServiceTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private LeaveGrantCalculator leaveGrantCalculator;

    private Clock clock;

    private LeaveBalanceServiceImpl service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
            ZonedDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        );
        leaveGrantCalculator = new LeaveGrantCalculator();
        service = new LeaveBalanceServiceImpl(
            leaveBalanceRepository, employeeRepository, leaveGrantCalculator, clock, eventPublisher
        );
    }

    @Nested
    @DisplayName("残高照会")
    class GetBalance {

        @Test
        @DisplayName("有効な残高のみが返され、合計残日数が正しく計算される")
        void getBalance_activeBalances_returnsSummary() {
            var balances = List.of(
                createBalance(2025, BigDecimal.TEN, BigDecimal.valueOf(3), BigDecimal.valueOf(7)),
                createBalance(2026, BigDecimal.valueOf(11), BigDecimal.ZERO, BigDecimal.valueOf(11))
            );
            when(leaveBalanceRepository.findActiveBalances(eq(1L), any(LocalDate.class)))
                .thenReturn(balances);

            var result = service.getBalance(1L);

            assertThat(result.employeeId()).isEqualTo(1L);
            assertThat(result.totalRemainingDays()).isEqualByComparingTo(BigDecimal.valueOf(18));
            assertThat(result.balances()).hasSize(2);
        }

        @Test
        @DisplayName("残高がない場合はゼロを返す")
        void getBalance_noBalances_returnsZero() {
            when(leaveBalanceRepository.findActiveBalances(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());

            var result = service.getBalance(1L);

            assertThat(result.totalRemainingDays()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.balances()).isEmpty();
        }
    }

    @Nested
    @DisplayName("残日数チェック")
    class HasEnoughBalance {

        @Test
        @DisplayName("十分な残高がある場合はtrue")
        void hasEnoughBalance_sufficient_returnsTrue() {
            var balances = List.of(
                createBalance(2025, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN)
            );
            when(leaveBalanceRepository.findActiveBalances(eq(1L), any(LocalDate.class)))
                .thenReturn(balances);

            assertThat(service.hasEnoughBalance(1L, BigDecimal.valueOf(5))).isTrue();
        }

        @Test
        @DisplayName("残高不足の場合はfalse")
        void hasEnoughBalance_insufficient_returnsFalse() {
            var balances = List.of(
                createBalance(2025, BigDecimal.TEN, BigDecimal.valueOf(8), BigDecimal.valueOf(2))
            );
            when(leaveBalanceRepository.findActiveBalances(eq(1L), any(LocalDate.class)))
                .thenReturn(balances);

            assertThat(service.hasEnoughBalance(1L, BigDecimal.valueOf(5))).isFalse();
        }
    }

    @Nested
    @DisplayName("有給消化（FIFO）")
    class Consume {

        @Test
        @DisplayName("古い年度から優先して消化される")
        void consume_multipleBalances_consumesFIFO() {
            var balance2025 = createBalance(2025, BigDecimal.TEN, BigDecimal.valueOf(7), BigDecimal.valueOf(3));
            var balance2026 = createBalance(2026, BigDecimal.valueOf(11), BigDecimal.ZERO, BigDecimal.valueOf(11));
            when(leaveBalanceRepository.findActiveBalances(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(balance2025, balance2026));

            ConsumeResult result = service.consume(1L, BigDecimal.valueOf(5));

            assertThat(result.totalConsumed()).isEqualByComparingTo(BigDecimal.valueOf(5));
            assertThat(result.details()).hasSize(2);
            assertThat(result.details().get(0).fiscalYear()).isEqualTo(2025);
            assertThat(result.details().get(0).consumed()).isEqualByComparingTo(BigDecimal.valueOf(3));
            assertThat(result.details().get(1).fiscalYear()).isEqualTo(2026);
            assertThat(result.details().get(1).consumed()).isEqualByComparingTo(BigDecimal.valueOf(2));
        }

        @Test
        @DisplayName("半日（0.5日）の消化ができる")
        void consume_halfDay_consumesCorrectly() {
            var balance = createBalance(2026, BigDecimal.valueOf(11), BigDecimal.ZERO, BigDecimal.valueOf(11));
            when(leaveBalanceRepository.findActiveBalances(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(balance));

            ConsumeResult result = service.consume(1L, BigDecimal.valueOf(0.5));

            assertThat(result.totalConsumed()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
        }

        @Test
        @DisplayName("残日数不足の場合は例外が発生する")
        void consume_insufficient_throwsException() {
            var balance = createBalance(2026, BigDecimal.valueOf(11), BigDecimal.TEN, BigDecimal.ONE);
            when(leaveBalanceRepository.findActiveBalances(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(balance));

            assertThatThrownBy(() -> service.consume(1L, BigDecimal.valueOf(5)))
                .isInstanceOf(com.example.attendance.common.exception.BusinessException.class);
        }
    }

    @Nested
    @DisplayName("一斉付与")
    class GrantAnnualLeave {

        @Test
        @DisplayName("適格社員に付与し、入社6ヶ月未満はスキップする")
        void grantAnnualLeave_mixedEligibility_grantsAndSkips() {
            var eligible = Employee.builder()
                .id(1L).employeeCode("E001").name("適格社員")
                .hireDate(LocalDate.of(2020, 4, 1)).active(true).role(Role.EMPLOYEE)
                .build();
            var ineligible = Employee.builder()
                .id(2L).employeeCode("E002").name("新入社員")
                .hireDate(LocalDate.of(2026, 2, 1)).active(true).role(Role.EMPLOYEE)
                .build();
            when(employeeRepository.findByActiveTrue()).thenReturn(List.of(eligible, ineligible));
            when(leaveBalanceRepository.existsByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(false);
            when(leaveBalanceRepository.existsByEmployeeIdAndFiscalYear(2L, 2026)).thenReturn(false);

            var result = service.grantAnnualLeave(2026);

            assertThat(result.granted()).isEqualTo(1);
            assertThat(result.skipped()).isEqualTo(1);
            assertThat(result.alreadyGranted()).isEqualTo(0);
            verify(leaveBalanceRepository, times(1)).save(any(LeaveBalance.class));
        }

        @Test
        @DisplayName("既に付与済みの社員はスキップされる")
        void grantAnnualLeave_alreadyGranted_skips() {
            var employee = Employee.builder()
                .id(1L).employeeCode("E001").name("テスト")
                .hireDate(LocalDate.of(2020, 4, 1)).active(true).role(Role.EMPLOYEE)
                .build();
            when(employeeRepository.findByActiveTrue()).thenReturn(List.of(employee));
            when(leaveBalanceRepository.existsByEmployeeIdAndFiscalYear(1L, 2026)).thenReturn(true);

            var result = service.grantAnnualLeave(2026);

            assertThat(result.granted()).isEqualTo(0);
            assertThat(result.alreadyGranted()).isEqualTo(1);
            verify(leaveBalanceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("失効処理")
    class Expire {

        @Test
        @DisplayName("失効レコードの残日数がゼロになる")
        void expireOutdatedBalances_setsRemainingToZero() {
            var expired = createBalance(2023, BigDecimal.TEN, BigDecimal.valueOf(3), BigDecimal.valueOf(7));
            when(leaveBalanceRepository.findExpiredWithRemaining(any(LocalDate.class)))
                .thenReturn(List.of(expired));

            int count = service.expireOutdatedBalances();

            assertThat(count).isEqualTo(1);
            assertThat(expired.getRemainingDays()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(expired.getUsedDays()).isEqualByComparingTo(BigDecimal.valueOf(3));
        }
    }

    private LeaveBalance createBalance(int fiscalYear, BigDecimal granted, BigDecimal used, BigDecimal remaining) {
        return LeaveBalance.builder()
            .id((long) fiscalYear)
            .employeeId(1L)
            .fiscalYear(fiscalYear)
            .grantedDays(granted)
            .usedDays(used)
            .remainingDays(remaining)
            .grantDate(LocalDate.of(fiscalYear, 4, 1))
            .expiryDate(LocalDate.of(fiscalYear + 2, 3, 31))
            .version(0L)
            .build();
    }
}
