package com.example.attendance.organization.repository;

import com.example.attendance.organization.entity.Organization;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrganizationRepositoryTest {

    @Autowired
    private OrganizationRepository repository;

    @Test
    @DisplayName("本部を保存して取得できる")
    void save_and_findById() {
        var org = Organization.builder()
                .name("営業本部")
                .code("SALES")
                .build();

        var saved = repository.save(org);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("コードで本部を検索できる")
    void findByCode_existing_returnsOrganization() {
        var org = Organization.builder()
                .name("技術本部")
                .code("TECH")
                .build();
        repository.save(org);

        var found = repository.findByCode("TECH");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("技術本部");
    }

    @Test
    @DisplayName("存在しないコードでは空が返される")
    void findByCode_notExisting_returnsEmpty() {
        assertThat(repository.findByCode("NONE")).isEmpty();
    }

    @Test
    @DisplayName("コードの存在チェックが正しく動作する")
    void existsByCode_returnsCorrectResult() {
        var org = Organization.builder()
                .name("管理本部")
                .code("ADMIN")
                .build();
        repository.save(org);

        assertThat(repository.existsByCode("ADMIN")).isTrue();
        assertThat(repository.existsByCode("NONE")).isFalse();
    }
}
