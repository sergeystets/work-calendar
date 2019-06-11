package com.google.work.calendar.service;

import com.google.work.calendar.dto.WorkingDay;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class WorkingCalendar {

    public List<WorkingDay> buildScheduleFor(final Pair<LocalDate, LocalDate> period) {
        Validate.notNull(period, "working period can not be null");
        final LocalDate from = period.getLeft();
        final LocalDate to = period.getRight();
        Validate.isTrue(from.isEqual(to) || to.isAfter(from), "invalid period is specified " + period + ", from should be >= to");

        final int totalDays = (int) ChronoUnit.DAYS.between(from, to) + 1;
        final List<WorkingDay> schedule = new ArrayList<>(totalDays);

        int daysCounter = 0;
        LocalDate date = from.minusDays(1);

        while (daysCounter <= totalDays) {

            int daysLeft = daysLeft(totalDays, daysCounter);
            if (daysLeft <= 0) {
                break;
            }

            // set up nigh shift
            for (int i = 0; i < Math.min(4, daysLeft); i++) {
                date = date.plusDays(1);
                schedule.add(WorkingDay.night(date));
                daysCounter++;
            }
            daysLeft = daysLeft(totalDays, daysCounter);
            if (daysLeft <= 0) {
                break;
            }

            // set up day off
            for (int i = 0; i < Math.min(2, daysLeft); i++) {
                date = date.plusDays(1);
                schedule.add(WorkingDay.dayOff(date));
                daysCounter++;
            }
            daysLeft = daysLeft(totalDays, daysCounter);
            if (daysLeft <= 0) {
                break;
            }

            // set up day shift
            for (int i = 0; i < Math.min(4, daysLeft); i++) {
                date = date.plusDays(1);
                schedule.add(WorkingDay.day(date));
                daysCounter++;
            }
            daysLeft = daysLeft(totalDays, daysCounter);
            if (daysLeft <= 0) {
                break;
            }

            // set up day off
            for (int i = 0; i < Math.min(1, daysLeft); i++) {
                date = date.plusDays(1);
                schedule.add(WorkingDay.dayOff(date));
                daysCounter++;
            }
            daysLeft = daysLeft(totalDays, daysCounter);
            if (daysLeft <= 0) {
                break;
            }

            // set up evening shift
            for (int i = 0; i < Math.min(4, daysLeft); i++) {
                date = date.plusDays(1);
                schedule.add(WorkingDay.evening(date));
                daysCounter++;
            }
            daysLeft = daysLeft(totalDays, daysCounter);
            if (daysLeft <= 0) {
                break;
            }

            // set up day off
            for (int i = 0; i < Math.min(1, daysLeft); i++) {
                date = date.plusDays(1);
                schedule.add(WorkingDay.dayOff(date));
                daysCounter++;
            }
        }

        return schedule;
    }

    private static int daysLeft(int total, int current) {
        return Math.max(0, total - current);
    }
}
