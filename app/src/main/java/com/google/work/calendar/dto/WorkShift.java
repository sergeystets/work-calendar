package com.google.work.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public enum WorkShift {

    NIGHT("Ночная смена", "ночь", LocalTime.of(0, 0), LocalTime.of(8, 0)),
    DAY("Дневная смена", "день", LocalTime.of(8, 0), LocalTime.of(16, 0)),
    EVENING("Вечерняя смена", "вечер", LocalTime.of(16, 0), LocalTime.of(23, 59)),
    DAY_OFF("Выходной", "вых", LocalTime.of(0, 0), LocalTime.of(23, 59));

    private String label;
    private String shortLabel;
    private LocalTime start;
    private LocalTime end;
}
