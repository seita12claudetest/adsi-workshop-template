package com.example.attendance.organization.repository;

import com.example.attendance.organization.entity.Department;
import com.example.attendance.organization.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Long organizationId;

    @BeforeEach
    void setUp() {
        var org = Organization.builder()
                .name("営業本部")
                .code("SALES")
                .build();
        organizationId = organizationRepository.save(org).getId();
    }

    @Test
    @DisplayName("本部IDで部一覧を取得できる")
    void findByOrganizationId_returnsDepartments() {
        departmentRepository.save(Department.builder()
                .organizationId(organizationId)
                .name("第一営業部")
                .code("SALES1")
                .build());
        departmentRepository.save(Department.builder()
                .organizationId(organizationId)
                .name("第二営業部")
                .code("SALES2")
                .build());

        var result = departmentRepository.findByOrganizationId(organizationId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("他の本部の部は含まれない")
    void findByOrganizationId_excludesOtherOrgs() {
        var otherOrg = organizationRepository.save(Organization.builder()
                .name("技術本部")
                .code("TECH")
                .build());
        departmentRepository.save(Department.builder()
                .organizationId(organizationId)
                .name("第一営業部")
                .code("SALES1")
                .build());
        departmentRepository.save(Department.builder()
                .organizationId(otherOrg.getId())
                .name("開発部")
                .code("DEV")
                .build());

        var result = departmentRepository.findByOrganizationId(organizationId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("第一営業部");
    }

    @Test
    @DisplayName("本部に部が存在するか確認できる")
    void existsByOrganizationId_returnsCorrectResult() {
        departmentRepository.save(Department.builder()
                .organizationId(organizationId)
                .name("第一営業部")
                .code("SALES1")
                .build());

        assertThat(departmentRepository.existsByOrganizationId(organizationId)).isTrue();
        assertThat(departmentRepository.existsByOrganizationId(999L)).isFalse();
    }
}
