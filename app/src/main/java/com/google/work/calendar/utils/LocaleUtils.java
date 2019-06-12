package com.google.work.calendar.utils;

import android.content.Context;

import java.util.Locale;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocaleUtils {

    public static Locale getLocaleFor(Context context) {
        return context.getResources().getConfiguration().getLocales().get(0);
    }
}
