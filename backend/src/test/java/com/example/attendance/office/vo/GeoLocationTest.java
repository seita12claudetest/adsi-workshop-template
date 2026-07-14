package com.example.attendance.office.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class GeoLocationTest {

    @Test
    @DisplayName("有効な緯度・経度でインスタンスが生成される")
    void of_validCoordinates_createsInstance() {
        var location = GeoLocation.of(35.6812, 139.7671);

        assertThat(location.latitude()).isEqualTo(35.6812);
        assertThat(location.longitude()).isEqualTo(139.7671);
    }

    @Test
    @DisplayName("緯度が-90未満の場合に例外がスローされる")
    void of_latitudeBelowMin_throwsException() {
        assertThatThrownBy(() -> GeoLocation.of(-91, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("緯度");
    }

    @Test
    @DisplayName("緯度が90超の場合に例外がスローされる")
    void of_latitudeAboveMax_throwsException() {
        assertThatThrownBy(() -> GeoLocation.of(91, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("緯度");
    }

    @Test
    @DisplayName("経度が-180未満の場合に例外がスローされる")
    void of_longitudeBelowMin_throwsException() {
        assertThatThrownBy(() -> GeoLocation.of(0, -181))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("経度");
    }

    @Test
    @DisplayName("経度が180超の場合に例外がスローされる")
    void of_longitudeAboveMax_throwsException() {
        assertThatThrownBy(() -> GeoLocation.of(0, 181))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("経度");
    }

    @Test
    @DisplayName("東京駅-渋谷駅間の距離が約6.4kmであること")
    void distanceTo_tokyoToShibuya_returnsApproximately6400m() {
        var tokyo = GeoLocation.of(35.6812, 139.7671);
        var shibuya = GeoLocation.of(35.6580, 139.7016);

        Distance distance = tokyo.distanceTo(shibuya);

        assertThat(distance.meters()).isCloseTo(6400, within(200.0));
    }

    @Test
    @DisplayName("同一地点間の距離は0である")
    void distanceTo_samePoint_returnsZero() {
        var point = GeoLocation.of(35.6812, 139.7671);

        Distance distance = point.distanceTo(point);

        assertThat(distance.meters()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("境界値（-90, -180）でインスタンスが生成される")
    void of_minBoundary_createsInstance() {
        var location = GeoLocation.of(-90, -180);

        assertThat(location.latitude()).isEqualTo(-90);
        assertThat(location.longitude()).isEqualTo(-180);
    }

    @Test
    @DisplayName("境界値（90, 180）でインスタンスが生成される")
    void of_maxBoundary_createsInstance() {
        var location = GeoLocation.of(90, 180);

        assertThat(location.latitude()).isEqualTo(90);
        assertThat(location.longitude()).isEqualTo(180);
    }
}
