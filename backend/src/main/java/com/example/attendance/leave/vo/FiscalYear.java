package com.example.attendance.leave.vo;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

public final class FiscalYear {

    private static final int FISCAL_YEAR_START_MONTH = 4;

    private final int year;

    private FiscalYear(int year) {
        this.year = year;
    }

    public static FiscalYear of(int year) {
        return new FiscalYear(year);
    }

    public static FiscalYear of(LocalDate date) {
        int y = date.getMonthValue() >= FISCAL_YEAR_START_MONTH
            ? date.getYear()
            : date.getYear() - 1;
        return new FiscalYear(y);
    }

    public static FiscalYear current(Clock clock) {
        return of(LocalDate.now(clock));
    }

    public int year() {
        return year;
    }

    public LocalDate grantDate() {
        return LocalDate.of(year, FISCAL_YEAR_START_MONTH, 1);
    }

    public LocalDate expiryDate() {
        return LocalDate.of(year + 2, 3, 31);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiscalYear that)) return false;
        return year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year);
    }

    @Override
    public String toString() {
        return "FiscalYear(" + year + ")";
    }
}
