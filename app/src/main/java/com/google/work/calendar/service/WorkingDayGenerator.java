package com.google.work.calendar.service;

import com.google.work.calendar.dto.WorkShift;
import com.google.work.calendar.dto.WorkingDay;

import java.time.LocalDate;

public interface WorkingDayGenerator {

    boolean isApplicable(WorkShift workShift);

    WorkingDay generate(LocalDate date);

    static WorkingDayGenerator nightShift() {
        return new WorkingDayGenerator() {
            @Override
            public boolean isApplicable(WorkShift workShift) {
                return workShift.isNight();
            }

            @Override
            public WorkingDay generate(LocalDate date) {
                return WorkingDay.night(date);
            }
        };
    }

    static WorkingDayGenerator dayShift() {
        return new WorkingDayGenerator() {
            @Override
            public boolean isApplicable(WorkShift workShift) {
                return workShift.isDay();
            }

            @Override
            public WorkingDay generate(LocalDate date) {
                return WorkingDay.day(date);
            }
        };
    }

    static WorkingDayGenerator eveningShift() {
        return new WorkingDayGenerator() {
            @Override
            public boolean isApplicable(WorkShift workShift) {
                return workShift.isEvening();
            }

            @Override
            public WorkingDay generate(LocalDate date) {
                return WorkingDay.evening(date);
            }
        };
    }

    static WorkingDayGenerator dayOff() {
        return new WorkingDayGenerator() {
            @Override
            public boolean isApplicable(WorkShift workShift) {
                return workShift.isDayOff();
            }

            @Override
            public WorkingDay generate(LocalDate date) {
                return WorkingDay.dayOff(date);
            }
        };
    }
}
