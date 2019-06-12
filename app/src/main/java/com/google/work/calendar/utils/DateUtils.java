package com.google.work.calendar.utils;

import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final TimeZone TIME_ZONE_UTC_PLUS_3 = TimeZone.getTimeZone("UTC+3");
}
