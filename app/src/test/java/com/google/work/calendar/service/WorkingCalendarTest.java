package com.google.work.calendar.service;

import com.google.work.calendar.dto.WorkShift;
import com.google.work.calendar.dto.WorkingDay;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public final class WorkingCalendarTest {

    private final WorkingCalendar workingCalendar = new WorkingCalendar();

    private final Pair<LocalDate, LocalDate> workingPeriod;
    private final List<WorkingDay> expectedSchedule;

    public WorkingCalendarTest(final Pair<LocalDate, LocalDate> workingPeriod,
                               final List<WorkingDay> expectedSchedule) {
        this.workingPeriod = workingPeriod;
        this.expectedSchedule = expectedSchedule;
    }

    @Parameterized.Parameters
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        // one cycle
                        Pair.of(LocalDate.of(2019, Month.MAY, 1), LocalDate.of(2019, Month.MAY, 16)),
                        Arrays.asList(
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 1)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 2)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 3)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 4)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 5)),
                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 6)),

                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 7)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 8)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 9)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 10)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 11)),

                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 12)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 13)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 14)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 15)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 16))
                        )
                },

                {
                        // two cycles
                        Pair.of(LocalDate.of(2019, Month.MAY, 1), LocalDate.of(2019, Month.JUNE, 1)),
                        Arrays.asList(
                                // the first cycle
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 1)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 2)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 3)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 4)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 5)),
                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 6)),

                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 7)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 8)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 9)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 10)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 11)),

                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 12)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 13)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 14)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 15)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 16)),

                                // the second cycle
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 17)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 18)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 19)),
                                WorkingDay.night(LocalDate.of(2019, Month.MAY, 20)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 21)),
                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 22)),

                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 23)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 24)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 25)),
                                WorkingDay.day(LocalDate.of(2019, Month.MAY, 26)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.MAY, 27)),

                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 28)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 29)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 30)),
                                WorkingDay.evening(LocalDate.of(2019, Month.MAY, 31)),

                                WorkingDay.dayOff(LocalDate.of(2019, Month.JUNE, 1))
                        )
                }
        });
    }

    @Test
    public void test() {
        final List<WorkingDay> actualSchedule = workingCalendar.buildScheduleFor(workingPeriod, WorkShift.NIGHT);
        assertThat(actualSchedule).isEqualTo(expectedSchedule);
    }
}