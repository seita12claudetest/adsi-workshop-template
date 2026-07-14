package com.example.attendance.employee.service;

import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.employee.dto.EmployeeRequest;
import com.example.attendance.employee.entity.Employee;
import com.example.attendance.employee.repository.EmployeeRepository;
import com.example.attendance.organization.entity.Section;
import com.example.attendance.organization.repository.SectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(employeeRepository, sectionRepository, passwordEncoder);
    }

    @Test
    @DisplayName("有効な社員一覧を取得できる")
    void findAll_returnsActiveEmployees() {
        var emp = Employee.builder()
                .id(1L).employeeCode("EMP001").name("田中太郎")
                .email("tanaka@example.com").role(Role.EMPLOYEE)
                .sectionId(1L).hireDate(LocalDate.of(2020, 4, 1)).active(true)
                .build();
        when(employeeRepository.findByActiveTrue()).thenReturn(List.of(emp));

        var result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("田中太郎");
    }

    @Test
    @DisplayName("IDで社員を取得できる")
    void findById_existingId_returnsEmployee() {
        var emp = Employee.builder()
                .id(1L).employeeCode("EMP001").name("田中太郎")
                .email("tanaka@example.com").role(Role.EMPLOYEE)
                .sectionId(1L).hireDate(LocalDate.of(2020, 4, 1)).active(true)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        var result = service.findById(1L);

        assertThat(result.name()).isEqualTo("田中太郎");
    }

    @Test
    @DisplayName("存在しないIDで検索するとエラーになる")
    void findById_notFound_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("社員を登録できる")
    void create_validRequest_createsEmployee() {
        var request = new EmployeeRequest(
                "EMP001", "田中太郎", "tanaka@example.com",
                "password123", "EMPLOYEE", 1L, LocalDate.of(2020, 4, 1));
        when(employeeRepository.existsByEmail("tanaka@example.com")).thenReturn(false);
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(sectionRepository.existsById(1L)).thenReturn(true);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(employeeRepository.save(any())).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        var result = service.create(request);

        assertThat(result.name()).isEqualTo("田中太郎");
        assertThat(result.employeeCode()).isEqualTo("EMP001");
    }

    @Test
    @DisplayName("重複メールで登録するとエラーになる")
    void create_duplicateEmail_throwsException() {
        var request = new EmployeeRequest(
                "EMP001", "田中太郎", "tanaka@example.com",
                "password123", "EMPLOYEE", 1L, LocalDate.of(2020, 4, 1));
        when(employeeRepository.existsByEmail("tanaka@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("社員を更新できる")
    void update_existingId_updatesEmployee() {
        var existing = Employee.builder()
                .id(1L).employeeCode("EMP001").name("田中太郎")
                .email("tanaka@example.com").role(Role.EMPLOYEE)
                .sectionId(1L).hireDate(LocalDate.of(2020, 4, 1)).active(true)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(sectionRepository.existsById(2L)).thenReturn(true);
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new EmployeeRequest(
                "EMP001", "田中次郎", "tanaka@example.com",
                null, "MANAGER", 2L, LocalDate.of(2020, 4, 1));
        var result = service.update(1L, request);

        assertThat(result.name()).isEqualTo("田中次郎");
        assertThat(result.role()).isEqualTo(Role.MANAGER);
    }

    @Test
    @DisplayName("社員を無効化できる")
    void deactivate_existingId_deactivatesEmployee() {
        var existing = Employee.builder()
                .id(1L).employeeCode("EMP001").name("田中太郎")
                .email("tanaka@example.com").role(Role.EMPLOYEE)
                .sectionId(1L).hireDate(LocalDate.of(2020, 4, 1)).active(true)
                .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deactivate(1L);

        assertThat(existing.isActive()).isFalse();
        verify(employeeRepository).save(existing);
    }

    @Test
    @DisplayName("社員の上長（課長）を取得できる")
    void getManager_returnsManager() {
        var emp = Employee.builder()
                .id(1L).sectionId(10L).active(true).build();
        var section = Section.builder().id(10L).managerId(5L).build();
        var manager = Employee.builder()
                .id(5L).employeeCode("MGR001").name("佐藤課長")
                .email("sato@example.com").role(Role.MANAGER)
                .sectionId(10L).hireDate(LocalDate.of(2015, 4, 1)).active(true)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(manager));

        var result = service.getManager(1L);

        assertThat(result.name()).isEqualTo("佐藤課長");
    }

    @Test
    @DisplayName("課長が未設定の場合エラーになる")
    void getManager_noManager_throwsException() {
        var emp = Employee.builder()
                .id(1L).sectionId(10L).active(true).build();
        var section = Section.builder().id(10L).managerId(null).build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(sectionRepository.findById(10L)).thenReturn(Optional.of(section));

        assertThatThrownBy(() -> service.getManager(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
