package com.example.attendance.leave.repository;

import com.example.attendance.common.enums.Role;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.leave.entity.LeaveBalance;
import com.example.attendance.organization.entity.Department;
import com.example.attendance.organization.entity.Organization;
import com.example.attendance.organization.entity.Section;
import com.example.attendance.organization.repository.DepartmentRepository;
import com.example.attendance.organization.repository.OrganizationRepository;
import com.example.attendance.organization.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LeaveBalanceRepositoryTest {

    @Autowired
    private LeaveBalanceRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    private Long employeeId;

    @BeforeEach
    void setUp() {
        var org = organizationRepository.save(Organization.builder()
            .name("テスト組織").code("TEST").build());
        var dept = departmentRepository.save(Department.builder()
            .name("テスト部").code("DEPT01").organizationId(org.getId()).build());
        var section = sectionRepository.save(Section.builder()
            .name("テスト課").code("SEC01").departmentId(dept.getId()).build());
        var employee = employeeRepository.save(Employee.builder()
            .employeeCode("EMP001").name("テスト社員").email("test@example.com")
            .password("encoded").role(Role.EMPLOYEE).sectionId(section.getId())
            .hireDate(LocalDate.of(2020, 4, 1)).active(true).build());
        employeeId = employee.getId();
    }

    @Test
    @DisplayName("有効な残高のみ取得される（期限内+残日数>0）")
    void findActiveBalances_onlyValidOnes_returned() {
        var active = LeaveBalance.builder()
            .employeeId(employeeId)
            .fiscalYear(2025)
            .grantedDays(BigDecimal.TEN)
            .usedDays(BigDecimal.valueOf(3))
            .remainingDays(BigDecimal.valueOf(7))
            .grantDate(LocalDate.of(2025, 4, 1))
            .expiryDate(LocalDate.of(2027, 3, 31))
            .build();

        var expired = LeaveBalance.builder()
            .employeeId(employeeId)
            .fiscalYear(2023)
            .grantedDays(BigDecimal.TEN)
            .usedDays(BigDecimal.valueOf(5))
            .remainingDays(BigDecimal.valueOf(5))
            .grantDate(LocalDate.of(2023, 4, 1))
            .expiryDate(LocalDate.of(2025, 3, 31))
            .build();

        var usedUp = LeaveBalance.builder()
            .employeeId(employeeId)
            .fiscalYear(2024)
            .grantedDays(BigDecimal.TEN)
            .usedDays(BigDecimal.TEN)
            .remainingDays(BigDecimal.ZERO)
            .grantDate(LocalDate.of(2024, 4, 1))
            .expiryDate(LocalDate.of(2026, 3, 31))
            .build();

        repository.save(active);
        repository.save(expired);
        repository.save(usedUp);

        var result = repository.findActiveBalances(employeeId, LocalDate.of(2026, 7, 14));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFiscalYear()).isEqualTo(2025);
    }

    @Test
    @DisplayName("FIFO順で返される（古い付与分が先）")
    void findActiveBalances_orderedByGrantDateAsc() {
        repository.save(LeaveBalance.builder()
            .employeeId(employeeId)
            .fiscalYear(2026)
            .grantedDays(BigDecimal.valueOf(11))
            .usedDays(BigDecimal.ZERO)
            .remainingDays(BigDecimal.valueOf(11))
            .grantDate(LocalDate.of(2026, 4, 1))
            .expiryDate(LocalDate.of(2028, 3, 31))
            .build());

        repository.save(LeaveBalance.builder()
            .employeeId(employeeId)
            .fiscalYear(2025)
            .grantedDays(BigDecimal.TEN)
            .usedDays(BigDecimal.valueOf(2))
            .remainingDays(BigDecimal.valueOf(8))
            .grantDate(LocalDate.of(2025, 4, 1))
            .expiryDate(LocalDate.of(2027, 3, 31))
            .build());

        var result = repository.findActiveBalances(employeeId, LocalDate.of(2026, 7, 14));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFiscalYear()).isEqualTo(2025);
        assertThat(result.get(1).getFiscalYear()).isEqualTo(2026);
    }

    @Test
    @DisplayName("同一社員・同一年度の重複チェック")
    void existsByEmployeeIdAndFiscalYear_existing_returnsTrue() {
        repository.save(LeaveBalance.builder()
            .employeeId(employeeId)
            .fiscalYear(2026)
            .grantedDays(BigDecimal.TEN)
            .usedDays(BigDecimal.ZERO)
            .remainingDays(BigDecimal.TEN)
            .grantDate(LocalDate.of(2026, 4, 1))
            .expiryDate(LocalDate.of(2028, 3, 31))
            .build());

        assertThat(repository.existsByEmployeeIdAndFiscalYear(employeeId, 2026)).isTrue();
        assertThat(repository.existsByEmployeeIdAndFiscalYear(employeeId, 2025)).isFalse();
    }

    @Test
    @DisplayName("失効済みで残日数ありのレコードを検索できる")
    void findExpiredWithRemaining_returnsExpiredOnly() {
        repository.save(LeaveBalance.builder()
            .employeeId(employeeId)
            .fiscalYear(2023)
            .grantedDays(BigDecimal.TEN)
            .usedDays(BigDecimal.valueOf(3))
            .remainingDays(BigDecimal.valueOf(7))
            .grantDate(LocalDate.of(2023, 4, 1))
            .expiryDate(LocalDate.of(2025, 3, 31))
            .build());

        repository.save(LeaveBalance.builder()
            .employeeId(employeeId)
            .fiscalYear(2025)
            .grantedDays(BigDecimal.TEN)
            .usedDays(BigDecimal.ZERO)
            .remainingDays(BigDecimal.TEN)
            .grantDate(LocalDate.of(2025, 4, 1))
            .expiryDate(LocalDate.of(2027, 3, 31))
            .build());

        var result = repository.findExpiredWithRemaining(LocalDate.of(2026, 7, 14));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFiscalYear()).isEqualTo(2023);
    }
}
