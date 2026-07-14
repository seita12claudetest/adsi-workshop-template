package com.example.attendance.attendance.vo;

import com.example.attendance.common.enums.TimeRecordType;

import java.util.Set;

public enum ClockState {
    NOT_CLOCKED_IN(Set.of(TimeRecordType.CLOCK_IN)),
    WORKING(Set.of(TimeRecordType.BREAK_START, TimeRecordType.CLOCK_OUT)),
    ON_BREAK(Set.of(TimeRecordType.BREAK_END, TimeRecordType.CLOCK_OUT)),
    CLOCKED_OUT(Set.of());

    private final Set<TimeRecordType> allowedTransitions;

    ClockState(Set<TimeRecordType> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransition(TimeRecordType type) {
        return allowedTransitions.contains(type);
    }

    public Set<TimeRecordType> getAllowedTransitions() {
        return Set.copyOf(allowedTransitions);
    }

    public ClockState next(TimeRecordType type) {
        if (!canTransition(type)) {
            throw new IllegalStateException(
                    "Cannot transition from " + this + " with " + type);
        }
        return switch (type) {
            case CLOCK_IN -> WORKING;
            case BREAK_START -> ON_BREAK;
            case BREAK_END -> WORKING;
            case CLOCK_OUT -> CLOCKED_OUT;
        };
    }
}
