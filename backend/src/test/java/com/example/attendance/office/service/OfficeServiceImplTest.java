package com.example.attendance.office.service;

import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.office.dto.NearestOfficeResponse;
import com.example.attendance.office.dto.OfficeRequest;
import com.example.attendance.office.dto.OfficeResponse;
import com.example.attendance.office.entity.Office;
import com.example.attendance.office.repository.OfficeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficeServiceImplTest {

    @Mock
    private OfficeRepository officeRepository;

    private OfficeServiceImpl officeService;

    @BeforeEach
    void setUp() {
        officeService = new OfficeServiceImpl(officeRepository);
    }

    private Office createOffice(Long id, String name, double lat, double lon, int radius) {
        return Office.builder()
                .id(id)
                .name(name)
                .address("テスト住所")
                .latitude(lat)
                .longitude(lon)
                .radiusMeters(radius)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("CRUD操作")
    class CrudTests {

        @Test
        @DisplayName("全拠点をページネーション付きで取得できる")
        void findAll_returnsPagedOffices() {
            var pageable = PageRequest.of(0, 20);
            var offices = List.of(
                    createOffice(1L, "本社", 35.6812, 139.7671, 500),
                    createOffice(2L, "大阪支社", 34.7024, 135.4959, 300)
            );
            when(officeRepository.findAll(pageable)).thenReturn(new PageImpl<>(offices, pageable, 2));

            var result = officeService.findAll(pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).name()).isEqualTo("本社");
        }

        @Test
        @DisplayName("有効なリクエストで拠点が作成される")
        void create_validRequest_createsOffice() {
            var request = new OfficeRequest("新拠点", "東京都新宿区1-1", 35.6896, 139.6922, 400);
            when(officeRepository.save(any(Office.class))).thenAnswer(inv -> {
                Office o = inv.getArgument(0);
                o.setId(1L);
                o.setCreatedAt(LocalDateTime.now());
                o.setUpdatedAt(LocalDateTime.now());
                return o;
            });

            OfficeResponse result = officeService.create(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("新拠点");
            assertThat(result.radiusMeters()).isEqualTo(400);
        }

        @Test
        @DisplayName("存在するIDで拠点が更新される")
        void update_existingId_updatesOffice() {
            var existing = createOffice(1L, "旧名", 35.0, 139.0, 200);
            var request = new OfficeRequest("新名", "新住所", 36.0, 140.0, 600);
            when(officeRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(officeRepository.save(any(Office.class))).thenAnswer(inv -> inv.getArgument(0));

            OfficeResponse result = officeService.update(1L, request);

            assertThat(result.name()).isEqualTo("新名");
            assertThat(result.latitude()).isEqualTo(36.0);
            assertThat(result.radiusMeters()).isEqualTo(600);
        }

        @Test
        @DisplayName("存在しないIDで更新するとNotFoundが投げられる")
        void update_nonExistingId_throwsNotFound() {
            var request = new OfficeRequest("名前", "住所", 35.0, 139.0, 200);
            when(officeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> officeService.update(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("存在するIDで拠点が削除される")
        void delete_existingId_deletesOffice() {
            when(officeRepository.existsById(1L)).thenReturn(true);

            officeService.delete(1L);

            verify(officeRepository).deleteById(1L);
        }

        @Test
        @DisplayName("存在しないIDで削除するとNotFoundが投げられる")
        void delete_nonExistingId_throwsNotFound() {
            when(officeRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> officeService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("最寄り拠点検索")
    class NearestOfficeTests {

        @Test
        @DisplayName("エリア内の場合withinAreaがtrueになる")
        void findNearest_withinRadius_returnsWithinTrue() {
            var office = createOffice(1L, "本社", 35.6812, 139.7671, 500);
            when(officeRepository.findAll()).thenReturn(List.of(office));

            NearestOfficeResponse result = officeService.findNearest(35.6815, 139.7675);

            assertThat(result.withinArea()).isTrue();
            assertThat(result.office().name()).isEqualTo("本社");
        }

        @Test
        @DisplayName("エリア外の場合withinAreaがfalseになる")
        void findNearest_outsideRadius_returnsWithinFalse() {
            var office = createOffice(1L, "本社", 35.6812, 139.7671, 100);
            when(officeRepository.findAll()).thenReturn(List.of(office));

            NearestOfficeResponse result = officeService.findNearest(35.69, 139.77);

            assertThat(result.withinArea()).isFalse();
        }

        @Test
        @DisplayName("複数拠点がある場合、最も近い拠点が返される")
        void findNearest_multipleOffices_returnsClosest() {
            var honsha = createOffice(1L, "本社", 35.6812, 139.7671, 500);
            var osaka = createOffice(2L, "大阪支社", 34.7024, 135.4959, 300);
            when(officeRepository.findAll()).thenReturn(List.of(honsha, osaka));

            NearestOfficeResponse result = officeService.findNearest(35.6820, 139.7680);

            assertThat(result.office().name()).isEqualTo("本社");
        }

        @Test
        @DisplayName("拠点が未登録の場合nullが返される")
        void findNearest_noOffices_returnsNull() {
            when(officeRepository.findAll()).thenReturn(List.of());

            NearestOfficeResponse result = officeService.findNearest(35.6812, 139.7671);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("距離がフォーマットされて返される")
        void findNearest_returnsFormattedDistance() {
            var office = createOffice(1L, "本社", 35.6812, 139.7671, 100);
            when(officeRepository.findAll()).thenReturn(List.of(office));

            NearestOfficeResponse result = officeService.findNearest(35.69, 139.77);

            assertThat(result.distanceFormatted()).isNotBlank();
            assertThat(result.distanceMeters()).isGreaterThan(0);
        }
    }
}
