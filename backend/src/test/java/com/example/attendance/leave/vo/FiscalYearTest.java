package com.example.attendance.leave.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FiscalYearTest {

    @ParameterizedTest
    @DisplayName("日付から会計年度を正しく判定する（4/1起算）")
    @CsvSource({
        "2026-04-01, 2026",
        "2026-03-31, 2025",
        "2026-12-31, 2026",
        "2027-01-01, 2026",
        "2027-03-31, 2026",
        "2027-04-01, 2027"
    })
    void of_date_returnsCorrectFiscalYear(LocalDate date, int expectedYear) {
        FiscalYear fy = FiscalYear.of(date);
        assertThat(fy.year()).isEqualTo(expectedYear);
    }

    @Test
    @DisplayName("付与日は会計年度の4/1")
    void grantDate_returnAprilFirst() {
        FiscalYear fy = FiscalYear.of(2026);
        assertThat(fy.grantDate()).isEqualTo(LocalDate.of(2026, 4, 1));
    }

    @Test
    @DisplayName("失効日は付与日から2年後の3/31")
    void expiryDate_returnsTwoYearsLater() {
        FiscalYear fy = FiscalYear.of(2026);
        assertThat(fy.expiryDate()).isEqualTo(LocalDate.of(2028, 3, 31));
    }

    @Test
    @DisplayName("Clockから現在の会計年度を取得できる")
    void current_clock_returnsCurrentFiscalYear() {
        Clock clock = Clock.fixed(
            ZonedDateTime.of(2026, 7, 14, 10, 0, 0, 0, ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        );
        FiscalYear fy = FiscalYear.current(clock);
        assertThat(fy.year()).isEqualTo(2026);
    }

    @Test
    @DisplayName("同じ年度は等価")
    void equals_sameYear_returnsTrue() {
        assertThat(FiscalYear.of(2026)).isEqualTo(FiscalYear.of(2026));
    }

    @Test
    @DisplayName("異なる年度は非等価")
    void equals_differentYear_returnsFalse() {
        assertThat(FiscalYear.of(2026)).isNotEqualTo(FiscalYear.of(2025));
    }
}
