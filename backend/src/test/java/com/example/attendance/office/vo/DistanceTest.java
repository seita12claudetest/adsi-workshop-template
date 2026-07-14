package com.example.attendance.office.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistanceTest {

    @Test
    @DisplayName("正の距離でインスタンスが生成される")
    void ofMeters_positiveValue_createsInstance() {
        var distance = Distance.ofMeters(500);

        assertThat(distance.meters()).isEqualTo(500);
    }

    @Test
    @DisplayName("0の距離でインスタンスが生成される")
    void ofMeters_zero_createsInstance() {
        var distance = Distance.ofMeters(0);

        assertThat(distance.meters()).isEqualTo(0);
    }

    @Test
    @DisplayName("負の距離で例外がスローされる")
    void ofMeters_negativeValue_throwsException() {
        assertThatThrownBy(() -> Distance.ofMeters(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("半径内ならisWithinがtrueを返す")
    void isWithin_insideRadius_returnsTrue() {
        var distance = Distance.ofMeters(300);

        assertThat(distance.isWithin(500)).isTrue();
    }

    @Test
    @DisplayName("半径と等しい距離ならisWithinがtrueを返す")
    void isWithin_exactlyOnBoundary_returnsTrue() {
        var distance = Distance.ofMeters(500);

        assertThat(distance.isWithin(500)).isTrue();
    }

    @Test
    @DisplayName("半径外ならisWithinがfalseを返す")
    void isWithin_outsideRadius_returnsFalse() {
        var distance = Distance.ofMeters(501);

        assertThat(distance.isWithin(500)).isFalse();
    }

    @Test
    @DisplayName("メートルからキロメートルへの変換が正しい")
    void toKilometers_convertsCorrectly() {
        var distance = Distance.ofMeters(6400);

        assertThat(distance.toKilometers()).isEqualTo(6.4);
    }

    @Test
    @DisplayName("1000m未満の場合はメートル表記")
    void toFormattedString_underOneKm_showsMeters() {
        var distance = Distance.ofMeters(320);

        assertThat(distance.toFormattedString()).isEqualTo("320m");
    }

    @Test
    @DisplayName("1000m以上の場合はキロメートル表記")
    void toFormattedString_overOneKm_showsKilometers() {
        var distance = Distance.ofMeters(1200);

        assertThat(distance.toFormattedString()).isEqualTo("1.2km");
    }

    @Test
    @DisplayName("ちょうど1000mの場合はキロメートル表記")
    void toFormattedString_exactlyOneKm_showsKilometers() {
        var distance = Distance.ofMeters(1000);

        assertThat(distance.toFormattedString()).isEqualTo("1.0km");
    }
}
