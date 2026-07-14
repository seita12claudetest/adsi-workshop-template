package com.example.attendance.organization.repository;

import com.example.attendance.organization.entity.Department;
import com.example.attendance.organization.entity.Organization;
import com.example.attendance.organization.entity.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SectionRepositoryTest {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Long departmentId;

    @BeforeEach
    void setUp() {
        var org = organizationRepository.save(Organization.builder()
                .name("営業本部")
                .code("SALES")
                .build());
        var dept = departmentRepository.save(Department.builder()
                .organizationId(org.getId())
                .name("第一営業部")
                .code("SALES1")
                .build());
        departmentId = dept.getId();
    }

    @Test
    @DisplayName("部IDで課一覧を取得できる")
    void findByDepartmentId_returnsSections() {
        sectionRepository.save(Section.builder()
                .departmentId(departmentId)
                .name("営業一課")
                .code("SALES1-1")
                .build());
        sectionRepository.save(Section.builder()
                .departmentId(departmentId)
                .name("営業二課")
                .code("SALES1-2")
                .build());

        var result = sectionRepository.findByDepartmentId(departmentId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("課長IDをnullで保存し、後から更新できる")
    void save_withoutManagerId_thenUpdate() {
        var section = sectionRepository.save(Section.builder()
                .departmentId(departmentId)
                .name("営業一課")
                .code("SALES1-1")
                .build());

        assertThat(section.getManagerId()).isNull();

        section.setManagerId(null);
        var updated = sectionRepository.save(section);

        assertThat(updated.getManagerId()).isNull();
    }

    @Test
    @DisplayName("部に課が存在するか確認できる")
    void existsByDepartmentId_returnsCorrectResult() {
        sectionRepository.save(Section.builder()
                .departmentId(departmentId)
                .name("営業一課")
                .code("SALES1-1")
                .build());

        assertThat(sectionRepository.existsByDepartmentId(departmentId)).isTrue();
        assertThat(sectionRepository.existsByDepartmentId(999L)).isFalse();
    }
}
