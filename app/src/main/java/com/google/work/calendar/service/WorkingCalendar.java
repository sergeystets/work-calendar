package com.google.work.calendar.service;

import com.google.work.calendar.dto.WorkShift;
import com.google.work.calendar.dto.WorkingDay;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkingCalendar {

    private static final List<WorkingDayGenerator> WORKING_DAY_GENERATORS = Arrays.asList(
            WorkingDayGenerator.nightShift(),
            WorkingDayGenerator.nightShift(),
            WorkingDayGenerator.nightShift(),
            WorkingDayGenerator.nightShift(),

            WorkingDayGenerator.dayOff(),
            WorkingDayGenerator.dayOff(),

            WorkingDayGenerator.dayShift(),
            WorkingDayGenerator.dayShift(),
            WorkingDayGenerator.dayShift(),
            WorkingDayGenerator.dayShift(),

            WorkingDayGenerator.dayOff(),

            WorkingDayGenerator.eveningShift(),
            WorkingDayGenerator.eveningShift(),
            WorkingDayGenerator.eveningShift(),
            WorkingDayGenerator.eveningShift(),

            WorkingDayGenerator.dayOff());

    public List<WorkingDay> buildScheduleFor(final Pair<LocalDate, LocalDate> period,
                                             final WorkShift workShift) {
        Validate.notNull(period, "working period can not be null");
        Validate.notNull(workShift, "workShift can not be null");
        final LocalDate from = period.getLeft();
        final LocalDate to = period.getRight();
        Validate.isTrue(from.isEqual(to) || to.isAfter(from), "invalid period is specified " + period + ", from should be >= to");

        final int totalDays = (int) ChronoUnit.DAYS.between(from, to) + 1;
        final List<WorkingDay> schedule = new ArrayList<>(totalDays);

        int daysCounter = 0;
        LocalDate date = from.minusDays(1);

        int i = findFirstGeneratorFor(workShift); // the first iteration may start from any arbitrary position
        while (daysCounter < totalDays) {
            for (; i < WORKING_DAY_GENERATORS.size() && daysCounter < totalDays; i++) {
                date = date.plusDays(1);
                WorkingDayGenerator workingDayGenerator = WORKING_DAY_GENERATORS.get(i);
                schedule.add(workingDayGenerator.generate(date));
                daysCounter++;
            }
            i = 0;
        }

        return schedule;
    }

    private static int findFirstGeneratorFor(WorkShift workShift) {
        for (int i = 0; i < WORKING_DAY_GENERATORS.size(); i++) {
            if (WORKING_DAY_GENERATORS.get(i).isApplicable(workShift)) {
                return i;
            }
        }
        return 0;
    }
}
