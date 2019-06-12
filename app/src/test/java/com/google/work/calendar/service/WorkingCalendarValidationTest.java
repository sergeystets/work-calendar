package com.google.work.calendar.service;

import com.google.work.calendar.dto.WorkShift;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class WorkingCalendarValidationTest {

    private WorkingCalendar workingCalendar = new WorkingCalendar();

    @Test
    public void shouldThrowExceptionIfPeriodIsNull() {
        assertThatThrownBy(() -> workingCalendar.buildScheduleFor(null, WorkShift.NIGHT))
                .hasMessage("working period can not be null")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldThrowExceptionIfFromIsAfterTo() {
        final LocalDate from = LocalDate.of(2019, 1, 2);
        final LocalDate to = LocalDate.of(2019, 1, 1);
        final Pair<LocalDate, LocalDate> workingPeriod = Pair.of(from, to);

        assertThatThrownBy(() -> workingCalendar.buildScheduleFor(workingPeriod, WorkShift.NIGHT))
                .hasMessage("invalid period is specified (2019-01-02,2019-01-01), from should be >= to")
                .isInstanceOf(IllegalArgumentException.class);
    }
}