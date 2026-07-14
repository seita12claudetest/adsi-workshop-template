package com.example.attendance.attendance.vo;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public record WorkingTime(int minutes) {

    public WorkingTime {
        if (minutes < 0) {
            minutes = 0;
        }
    }

    public static final int STANDARD_WORKING_MINUTES = 435;
    public static final int DEFAULT_BREAK_MINUTES = 60;

    public static WorkingTime of(int minutes) {
        return new WorkingTime(minutes);
    }

    public static WorkingTime between(LocalTime from, LocalTime to) {
        if (from == null || to == null) {
            return new WorkingTime(0);
        }
        int mins = (int) ChronoUnit.MINUTES.between(from, to);
        return new WorkingTime(Math.max(0, mins));
    }

    public WorkingTime add(WorkingTime other) {
        return new WorkingTime(this.minutes + other.minutes);
    }

    public WorkingTime subtract(WorkingTime other) {
        return new WorkingTime(this.minutes - other.minutes);
    }

    public int overtimeMinutes() {
        return Math.max(0, minutes - STANDARD_WORKING_MINUTES);
    }

    public String toFormattedString() {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%d:%02d", hours, mins);
    }
}
