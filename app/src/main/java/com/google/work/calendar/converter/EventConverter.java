package com.google.work.calendar.converter;

import com.google.common.collect.ImmutableMap;
import com.google.work.calendar.dto.CalendarEvent;
import com.google.work.calendar.dto.WorkShift;
import com.google.work.calendar.dto.WorkingDay;
import com.google.work.calendar.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EventConverter {

    private final static Map<WorkShift, Integer> colors = ImmutableMap.of(
            WorkShift.NIGHT, 1,     // blue
            WorkShift.DAY, 5,       // yellow
            WorkShift.EVENING, 10,  // green
            WorkShift.DAY_OFF, 11); // red

    public List<CalendarEvent> convert(Locale locale, final List<WorkingDay> workingDays) {
        final List<CalendarEvent> events = new ArrayList<>();

        for (WorkingDay workingDay : workingDays) {
            events.add(CalendarEvent.builder()
                    .color(colors.get(workingDay.getWorkShift()))
                    .from(LocalDateTime.of(workingDay.getDate(), workingDay.getWorkShift().getStart()))
                    .to(LocalDateTime.of(workingDay.getDate(), workingDay.getWorkShift().getEnd()))
                    .name(workingDay.getWorkShift().getShortLabel().toLowerCase())
                    .description(workingDay.getDate().format(DateUtils.DATE_TIME_FORMATTER.withLocale(locale)) + " - " +
                            workingDay
                                    .getWorkShift()
                                    .getShortLabel()
                                    .toLowerCase())
                    .build());
        }

        return events;
    }
}
