package com.example.attendance.attendance.vo;

import com.example.attendance.common.enums.TimeRecordType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClockStateTest {

    @Test
    @DisplayName("NOT_CLOCKED_IN → CLOCK_IN → WORKING")
    void notClockedIn_clockIn_becomesWorking() {
        assertThat(ClockState.NOT_CLOCKED_IN.canTransition(TimeRecordType.CLOCK_IN)).isTrue();
        assertThat(ClockState.NOT_CLOCKED_IN.next(TimeRecordType.CLOCK_IN)).isEqualTo(ClockState.WORKING);
    }

    @Test
    @DisplayName("WORKING → BREAK_START → ON_BREAK")
    void working_breakStart_becomesOnBreak() {
        assertThat(ClockState.WORKING.next(TimeRecordType.BREAK_START)).isEqualTo(ClockState.ON_BREAK);
    }

    @Test
    @DisplayName("ON_BREAK → BREAK_END → WORKING")
    void onBreak_breakEnd_becomesWorking() {
        assertThat(ClockState.ON_BREAK.next(TimeRecordType.BREAK_END)).isEqualTo(ClockState.WORKING);
    }

    @Test
    @DisplayName("WORKING → CLOCK_OUT → CLOCKED_OUT")
    void working_clockOut_becomesClockedOut() {
        assertThat(ClockState.WORKING.next(TimeRecordType.CLOCK_OUT)).isEqualTo(ClockState.CLOCKED_OUT);
    }

    @Test
    @DisplayName("ON_BREAK → CLOCK_OUT → CLOCKED_OUT（休憩戻り忘れ救済）")
    void onBreak_clockOut_becomesClockedOut() {
        assertThat(ClockState.ON_BREAK.next(TimeRecordType.CLOCK_OUT)).isEqualTo(ClockState.CLOCKED_OUT);
    }

    @Test
    @DisplayName("NOT_CLOCKED_IN → CLOCK_OUT は不正遷移")
    void notClockedIn_clockOut_throwsException() {
        assertThat(ClockState.NOT_CLOCKED_IN.canTransition(TimeRecordType.CLOCK_OUT)).isFalse();
        assertThatThrownBy(() -> ClockState.NOT_CLOCKED_IN.next(TimeRecordType.CLOCK_OUT))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("CLOCKED_OUT → いかなる遷移も不可")
    void clockedOut_noTransitionAllowed() {
        assertThat(ClockState.CLOCKED_OUT.canTransition(TimeRecordType.CLOCK_IN)).isFalse();
        assertThat(ClockState.CLOCKED_OUT.canTransition(TimeRecordType.BREAK_START)).isFalse();
    }

    @Test
    @DisplayName("WORKING → CLOCK_IN は不正遷移")
    void working_clockIn_throwsException() {
        assertThatThrownBy(() -> ClockState.WORKING.next(TimeRecordType.CLOCK_IN))
                .isInstanceOf(IllegalStateException.class);
    }
}
