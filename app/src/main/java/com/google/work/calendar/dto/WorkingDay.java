package com.google.work.calendar.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class WorkingDay {

    private WorkShift workShift;
    private LocalDate date;

    public static WorkingDay night(LocalDate date) {
        return WorkingDay.builder().workShift(WorkShift.NIGHT).date(date).build();
    }

    public static WorkingDay dayOff(LocalDate date) {
        return WorkingDay.builder().workShift(WorkShift.DAY_OFF).date(date).build();
    }

    public static WorkingDay evening(LocalDate date) {
        return WorkingDay.builder().workShift(WorkShift.EVENING).date(date).build();
    }

    public static WorkingDay day(LocalDate date) {
        return WorkingDay.builder().workShift(WorkShift.DAY).date(date).build();
    }

}
