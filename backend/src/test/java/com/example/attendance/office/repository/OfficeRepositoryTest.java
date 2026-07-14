package com.example.attendance.office.repository;

import com.example.attendance.office.entity.Office;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OfficeRepositoryTest {

    @Autowired
    private OfficeRepository officeRepository;

    @Test
    @DisplayName("拠点を保存して読み出せる")
    void save_validEntity_persists() {
        var office = Office.builder()
                .name("本社")
                .address("東京都千代田区丸の内1-1-1")
                .latitude(35.6812)
                .longitude(139.7671)
                .radiusMeters(500)
                .build();

        var saved = officeRepository.save(office);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("本社");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("複数拠点を保存して全件取得できる")
    void findAll_multipleEntities_returnsAll() {
        officeRepository.save(Office.builder()
                .name("本社")
                .address("東京都千代田区丸の内1-1-1")
                .latitude(35.6812)
                .longitude(139.7671)
                .radiusMeters(500)
                .build());
        officeRepository.save(Office.builder()
                .name("大阪支社")
                .address("大阪府大阪市北区梅田1-1-1")
                .latitude(34.7024)
                .longitude(135.4959)
                .radiusMeters(300)
                .build());

        var offices = officeRepository.findAll();

        assertThat(offices).hasSize(2);
    }

    @Test
    @DisplayName("拠点を削除できる")
    void delete_existingEntity_removes() {
        var office = officeRepository.save(Office.builder()
                .name("テスト拠点")
                .address("テスト住所")
                .latitude(35.0)
                .longitude(139.0)
                .radiusMeters(100)
                .build());

        officeRepository.deleteById(office.getId());

        assertThat(officeRepository.findById(office.getId())).isEmpty();
    }
}
