package com.example.attendance.attendance.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingTimeTest {

    @Test
    @DisplayName("分数から時間:分のフォーマットに変換できる")
    void toFormattedString_convertsCorrectly() {
        assertThat(WorkingTime.of(435).toFormattedString()).isEqualTo("7:15");
        assertThat(WorkingTime.of(60).toFormattedString()).isEqualTo("1:00");
        assertThat(WorkingTime.of(0).toFormattedString()).isEqualTo("0:00");
        assertThat(WorkingTime.of(125).toFormattedString()).isEqualTo("2:05");
    }

    @Test
    @DisplayName("2つの WorkingTime を加算できる")
    void add_combinesTwoWorkingTimes() {
        var a = WorkingTime.of(200);
        var b = WorkingTime.of(235);
        assertThat(a.add(b).minutes()).isEqualTo(435);
    }

    @Test
    @DisplayName("減算で負にならない（下限0）")
    void subtract_neverNegative() {
        var a = WorkingTime.of(30);
        var b = WorkingTime.of(60);
        assertThat(a.subtract(b).minutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("2つの LocalTime 間の分数を計算できる")
    void between_calculatesMinutesBetweenTimes() {
        var from = LocalTime.of(9, 0);
        var to = LocalTime.of(17, 30);
        assertThat(WorkingTime.between(from, to).minutes()).isEqualTo(510);
    }

    @Test
    @DisplayName("null の場合は 0 分を返す")
    void between_returnsZeroForNull() {
        assertThat(WorkingTime.between(null, LocalTime.of(17, 0)).minutes()).isEqualTo(0);
        assertThat(WorkingTime.between(LocalTime.of(9, 0), null).minutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("残業時間: 435分以下は0分、超過分が残業")
    void overtimeMinutes_calculatesCorrectly() {
        assertThat(WorkingTime.of(435).overtimeMinutes()).isEqualTo(0);
        assertThat(WorkingTime.of(400).overtimeMinutes()).isEqualTo(0);
        assertThat(WorkingTime.of(540).overtimeMinutes()).isEqualTo(105);
    }
}
