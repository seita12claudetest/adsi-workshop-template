package com.example.attendance.organization.service;

import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.organization.dto.*;
import com.example.attendance.organization.entity.Department;
import com.example.attendance.organization.entity.Organization;
import com.example.attendance.organization.entity.Section;
import com.example.attendance.organization.repository.DepartmentRepository;
import com.example.attendance.organization.repository.OrganizationRepository;
import com.example.attendance.organization.repository.SectionRepository;
import com.example.attendance.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private OrganizationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrganizationServiceImpl(
                organizationRepository, departmentRepository, sectionRepository, employeeRepository);
    }

    @Nested
    @DisplayName("本部 CRUD")
    class OrganizationCrud {

        @Test
        @DisplayName("本部一覧を取得できる")
        void findAll_returnsAllOrganizations() {
            var org1 = Organization.builder().id(1L).name("営業本部").code("SALES").build();
            var org2 = Organization.builder().id(2L).name("技術本部").code("TECH").build();
            when(organizationRepository.findAll()).thenReturn(List.of(org1, org2));

            var result = service.findAllOrganizations();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo("営業本部");
        }

        @Test
        @DisplayName("本部を作成できる")
        void create_validRequest_createsOrganization() {
            var request = new OrganizationRequest("営業本部", "SALES");
            when(organizationRepository.existsByCode("SALES")).thenReturn(false);
            when(organizationRepository.save(any())).thenAnswer(inv -> {
                Organization o = inv.getArgument(0);
                o.setId(1L);
                return o;
            });

            var result = service.createOrganization(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("営業本部");
            assertThat(result.code()).isEqualTo("SALES");
        }

        @Test
        @DisplayName("重複コードで作成するとエラーになる")
        void create_duplicateCode_throwsException() {
            var request = new OrganizationRequest("営業本部", "SALES");
            when(organizationRepository.existsByCode("SALES")).thenReturn(true);

            assertThatThrownBy(() -> service.createOrganization(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("本部を更新できる")
        void update_existingId_updatesOrganization() {
            var existing = Organization.builder().id(1L).name("旧営業本部").code("SALES").build();
            when(organizationRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(organizationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var request = new OrganizationRequest("新営業本部", "SALES");
            var result = service.updateOrganization(1L, request);

            assertThat(result.name()).isEqualTo("新営業本部");
        }

        @Test
        @DisplayName("存在しない本部を更新するとエラーになる")
        void update_notFound_throwsException() {
            when(organizationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateOrganization(999L, new OrganizationRequest("x", "X")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("本部を削除できる")
        void delete_existingId_deletesOrganization() {
            when(organizationRepository.existsById(1L)).thenReturn(true);
            when(departmentRepository.existsByOrganizationId(1L)).thenReturn(false);

            service.deleteOrganization(1L);

            verify(organizationRepository).deleteById(1L);
        }

        @Test
        @DisplayName("部が存在する本部は削除できない")
        void delete_hasDepartments_throwsException() {
            when(organizationRepository.existsById(1L)).thenReturn(true);
            when(departmentRepository.existsByOrganizationId(1L)).thenReturn(true);

            assertThatThrownBy(() -> service.deleteOrganization(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("部 CRUD")
    class DepartmentCrud {

        @Test
        @DisplayName("本部IDで部一覧を取得できる")
        void findByOrganizationId_returnsDepartments() {
            var dept = Department.builder().id(1L).organizationId(1L).name("第一営業部").code("SALES1").build();
            when(departmentRepository.findByOrganizationId(1L)).thenReturn(List.of(dept));

            var result = service.findDepartmentsByOrganizationId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("第一営業部");
        }

        @Test
        @DisplayName("部を作成できる")
        void create_validRequest_createsDepartment() {
            var request = new DepartmentRequest(1L, "第一営業部", "SALES1");
            when(organizationRepository.existsById(1L)).thenReturn(true);
            when(departmentRepository.existsByCode("SALES1")).thenReturn(false);
            when(departmentRepository.save(any())).thenAnswer(inv -> {
                Department d = inv.getArgument(0);
                d.setId(1L);
                return d;
            });

            var result = service.createDepartment(request);

            assertThat(result.name()).isEqualTo("第一営業部");
        }

        @Test
        @DisplayName("存在しない本部IDで部を作成するとエラーになる")
        void create_invalidOrganizationId_throwsException() {
            var request = new DepartmentRequest(999L, "部", "DEPT");
            when(organizationRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.createDepartment(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("部を更新できる")
        void update_existingId_updatesDepartment() {
            var existing = Department.builder().id(1L).organizationId(1L).name("旧部").code("SALES1").build();
            when(departmentRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(departmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = service.updateDepartment(1L, new DepartmentRequest(1L, "新部", "SALES1"));

            assertThat(result.name()).isEqualTo("新部");
        }

        @Test
        @DisplayName("課が存在する部は削除できない")
        void delete_hasSections_throwsException() {
            when(departmentRepository.existsById(1L)).thenReturn(true);
            when(sectionRepository.existsByDepartmentId(1L)).thenReturn(true);

            assertThatThrownBy(() -> service.deleteDepartment(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("課 CRUD")
    class SectionCrud {

        @Test
        @DisplayName("部IDで課一覧を取得できる")
        void findByDepartmentId_returnsSections() {
            var section = Section.builder().id(1L).departmentId(1L).name("営業一課").code("S1-1").build();
            when(sectionRepository.findByDepartmentId(1L)).thenReturn(List.of(section));

            var result = service.findSectionsByDepartmentId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("営業一課");
        }

        @Test
        @DisplayName("課を作成できる")
        void create_validRequest_createsSection() {
            var request = new SectionRequest(1L, "営業一課", "S1-1", null);
            when(departmentRepository.existsById(1L)).thenReturn(true);
            when(sectionRepository.existsByCode("S1-1")).thenReturn(false);
            when(sectionRepository.save(any())).thenAnswer(inv -> {
                Section s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            var result = service.createSection(request);

            assertThat(result.name()).isEqualTo("営業一課");
        }

        @Test
        @DisplayName("課を更新できる（課長を設定）")
        void update_setManager_updatesSection() {
            var existing = Section.builder().id(1L).departmentId(1L).name("営業一課").code("S1-1").build();
            when(sectionRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(employeeRepository.existsById(10L)).thenReturn(true);
            when(sectionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var request = new SectionRequest(1L, "営業一課", "S1-1", 10L);
            var result = service.updateSection(1L, request);

            assertThat(result.managerId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("社員が所属する課は削除できない")
        void delete_hasEmployees_throwsException() {
            when(sectionRepository.existsById(1L)).thenReturn(true);
            when(employeeRepository.existsBySectionId(1L)).thenReturn(true);

            assertThatThrownBy(() -> service.deleteSection(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
