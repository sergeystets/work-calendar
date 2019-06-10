package com.google.work.calendar;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import static com.google.work.calendar.MainActivity.USER_DISPLAY_NAME;


public class SetupScheduleActivity extends AppCompatActivity {

    private static final String TAG = "SetupScheduleActivity";
    private static final String APPLICATION_NAME = "Work Calendar App";
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    private GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_schedule);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(CALENDAR_SCOPE))
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_out_button).setOnClickListener(v -> signOut());

        findViewById(R.id.build_schedule).setOnClickListener(v -> buildSchedule());

        String userName = getIntent().getStringExtra(USER_DISPLAY_NAME);
        TextView hiTextView = findViewById(R.id.hi_dialog_text_view);
        hiTextView.setText(getString(R.string.say_hi, StringUtils.defaultString(userName, "<anonymous>")));
    }

    private void signOut() {
        signInClient.signOut().addOnCompleteListener(task -> finish());
    }

    private void buildSchedule() {
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acc != null) {
            new BuildScheduleTask(this).execute(acc.getAccount());
        } else {
            Toast.makeText(this, R.string.please_sign_in, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private static class BuildScheduleTask extends AsyncTask<Account, Void, Void> {

        private WeakReference<SetupScheduleActivity> setupScheduleActivity;

        BuildScheduleTask(SetupScheduleActivity setupScheduleActivity) {
            this.setupScheduleActivity = new WeakReference<>(setupScheduleActivity);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Context context = setupScheduleActivity.get().getApplicationContext();
            Toast.makeText(context, R.string.schedule_was_built, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Account... accounts) {
            Context context = setupScheduleActivity.get().getApplicationContext();
            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        context,
                        Collections.singleton(CALENDAR_SCOPE));

                credential.setSelectedAccount(accounts[0]);

                Calendar calendarApi = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();

                CalendarList calendarList = calendarApi.calendarList().list().execute();
                List<CalendarListEntry> items = calendarList.getItems();
                Log.w(TAG, "Got " + items.size() + " calendars");
            } catch (Exception e) {
                Log.w(TAG, "buildSchedule:error", e);
            }

            return null;
        }
    }
}
