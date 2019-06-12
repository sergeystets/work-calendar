package com.google.work.calendar.async;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.work.calendar.MainActivity;
import com.google.work.calendar.R;
import com.google.work.calendar.SetupScheduleActivity;
import com.google.work.calendar.converter.EventConverter;
import com.google.work.calendar.dto.CalendarEvent;
import com.google.work.calendar.dto.WorkShift;
import com.google.work.calendar.dto.WorkingDay;
import com.google.work.calendar.service.WorkingCalendar;
import com.google.work.calendar.utils.LocaleUtils;
import com.google.work.calendar.utils.Scope;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.work.calendar.utils.DateUtils.TIME_ZONE_UTC_PLUS_3;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public class BuildScheduleTask extends AsyncTask<Account, Void, Void> {

    private static final String TAG = "BuildScheduleTask";
    private static final String CALENDAR_NAME_PREFIX = "working_calendar_for_brigade_";
    private static final WorkingCalendar workingCalendar = new WorkingCalendar();
    private static final EventConverter eventConverter = new EventConverter();
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    private final int brigade;
    private final LocalDate startFrom;
    private final WorkShift workShift;
    private final WeakReference<SetupScheduleActivity> setupScheduleActivity;

    public BuildScheduleTask(final SetupScheduleActivity setupScheduleActivity,
                             final int brigade,
                             final LocalDate startFrom,
                             final WorkShift workShift) {
        this.setupScheduleActivity = new WeakReference<>(setupScheduleActivity);
        this.brigade = brigade;
        this.startFrom = startFrom;
        this.workShift = workShift;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        SetupScheduleActivity setupScheduleActivity = this.setupScheduleActivity.get();
        if (setupScheduleActivity != null) {
            Context context = setupScheduleActivity.getApplicationContext();
            setupScheduleActivity.hideProgressDialog();
            Toast.makeText(context, R.string.schedule_was_built, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected Void doInBackground(Account... accounts) {
        if (setupScheduleActivity.get() == null) {
            return null;
        }
        Context context = this.setupScheduleActivity.get().getApplicationContext();
        try {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton(Scope.CALENDAR_SCOPE));

            credential.setSelectedAccount(accounts[0]);

            Calendar calendarApi = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(MainActivity.APPLICATION_NAME)
                    .build();

            String calendarId = getOrCreateCalendar(calendarApi, CALENDAR_NAME_PREFIX + brigade);

            final Pair<LocalDate, LocalDate> period = Pair.of(
                    startFrom,
                    startFrom.with(lastDayOfMonth()));
            final Locale locale = LocaleUtils.getLocaleFor(context);
            final List<WorkingDay> workingDays = workingCalendar.buildScheduleFor(period, workShift);
            final List<CalendarEvent> events = eventConverter.convert(locale, workingDays);

            for (final CalendarEvent event : events) {
                final Event apiEvent = convert(event);
                Log.w(TAG, "Inserting new calendar event ->" + apiEvent);
                calendarApi.events().insert(calendarId, apiEvent).execute();
            }
        } catch (UserRecoverableAuthIOException e) {
            if (setupScheduleActivity.get() != null) {
                setupScheduleActivity.get().onRecoverableAuthException(e);
            }
        } catch (IOException e) {
            Log.w(TAG, "buildSchedule:error", e);
        }
        return null;
    }

    private String getOrCreateCalendar(Calendar calendarApi, String calendarName) throws IOException {
        CalendarListEntry existingCalendar = calendarApi
                .calendarList()
                .list()
                .execute()
                .getItems()
                .stream()
                .filter(c -> c.getSummary().equals(calendarName))
                .findAny()
                .orElse(null);

        String calendarId;
        if (existingCalendar == null) {
            com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
            calendar.setSummary(calendarName);
            com.google.api.services.calendar.model.Calendar newCalendar = calendarApi.calendars().insert(calendar).execute();
            calendarId = newCalendar.getId();
        } else {
            calendarId = existingCalendar.getId();
        }

        return calendarId;
    }

    private static Event convert(final CalendarEvent event) {
        final Event apiEvent = new Event();
        apiEvent.setColorId(String.valueOf(event.getColor()));
        final EventDateTime start = new EventDateTime().
                setDateTime(new DateTime(
                        Date.from(event.getFrom().atZone(ZoneId.systemDefault()).toInstant()),
                        TIME_ZONE_UTC_PLUS_3));
        final EventDateTime end = new EventDateTime().
                setDateTime(new DateTime(
                        Date.from(event.getTo().atZone(ZoneId.systemDefault()).toInstant()),
                        TIME_ZONE_UTC_PLUS_3));

        apiEvent.setStart(start);
        apiEvent.setEnd(end);
        apiEvent.setSummary(event.getName());
        apiEvent.setDescription(event.getDescription());

        return apiEvent;
    }
}
