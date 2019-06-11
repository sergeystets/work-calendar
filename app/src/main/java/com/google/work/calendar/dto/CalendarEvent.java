package com.google.work.calendar.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarEvent {

    private LocalDateTime from;
    private LocalDateTime to;
    private int color;
    private String name;
    private String description;
}
