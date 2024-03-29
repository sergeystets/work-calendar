package com.google.work.calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.common.collect.ImmutableMap;
import com.google.work.calendar.async.BuildScheduleTask;
import com.google.work.calendar.dto.WorkShift;
import com.google.work.calendar.utils.DateUtils;
import com.google.work.calendar.utils.LocaleUtils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import static com.google.work.calendar.MainActivity.USER_DISPLAY_NAME;
import static com.google.work.calendar.utils.Status.RC_RECOVERABLE;


public class SetupScheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "SetupScheduleActivity";
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";
    private static final Map<Integer, WorkShift> WORK_SHIFTS = ImmutableMap.of(
            0, WorkShift.DAY,
            1, WorkShift.NIGHT,
            2, WorkShift.EVENING);

    private static final Map<WorkShift, Integer> WORK_SHIFT_LABELS = ImmutableMap.of(
            WorkShift.DAY, R.string.day_shift,
            WorkShift.NIGHT, R.string.night_shift,
            WorkShift.EVENING, R.string.evening_shift);

    private GoogleSignInClient signInClient;

    private ProgressDialog progressDialog;

    private int brigadeNumber = 1;
    private LocalDate startFrom;
    private WorkShift workShift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_schedule);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(CALENDAR_SCOPE))
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.build_schedule).setOnClickListener(v -> openConfirmDialog());

        String userName = getIntent().getStringExtra(USER_DISPLAY_NAME);
        TextView hiTextView = findViewById(R.id.hi_dialog_text_view);
        hiTextView.setText(getString(R.string.say_hi, StringUtils.defaultString(userName, "<anonymous>")));

        Spinner brigadeSpinner = findViewById(R.id.brigade_spinner);
        ArrayAdapter<CharSequence> brigadeAdapter = ArrayAdapter.createFromResource(this,
                R.array.brigade, android.R.layout.simple_spinner_item);
        brigadeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brigadeSpinner.setAdapter(brigadeAdapter);
        brigadeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBrigade = parent.getItemAtPosition(position).toString();
                brigadeNumber = Integer.valueOf(StringUtils.defaultIfBlank(selectedBrigade, "1"));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        Spinner workShiftSpinner = findViewById(R.id.work_shift_spinner);
        ArrayAdapter<CharSequence> workingShiftAdapter = ArrayAdapter.createFromResource(this,
                R.array.work_shift, android.R.layout.simple_spinner_item);
        workingShiftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workShiftSpinner.setAdapter(workingShiftAdapter);
        workShiftSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                workShift = ObjectUtils.defaultIfNull(WORK_SHIFTS.get(position), WorkShift.DAY);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        Locale locale = LocaleUtils.getLocaleFor(getApplicationContext());
        TextView selectedDate = findViewById(R.id.selected_date);
        this.startFrom = LocalDate.now();
        selectedDate.setText(this.startFrom.format(DateUtils.DATE_TIME_FORMATTER.withLocale(locale)));
    }

    private void openConfirmDialog() {
        Locale locale = LocaleUtils.getLocaleFor(getApplicationContext());
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.create_schedule_confirmation_dialog_title))
                .setMessage(getString(
                        R.string.create_schedule_confirmation_dialog_message,
                        String.valueOf(brigadeNumber),
                        StringUtils.capitalize(startFrom.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale)),
                        getString(WORK_SHIFT_LABELS.get(workShift)),
                        startFrom.format(DateUtils.DATE_TIME_FORMATTER.withLocale(locale))))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> buildSchedule())
                .setNegativeButton(android.R.string.no, null).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_RECOVERABLE) {
            if (resultCode == RESULT_OK) {
                buildSchedule();
            } else {
                Toast.makeText(this, R.string.failed_to_build_schedule, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        signInClient.signOut().addOnCompleteListener(task -> finish());
    }

    private void buildSchedule() {
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acc != null) {
            showProgressDialog();
            new BuildScheduleTask(this,
                    brigadeNumber,
                    startFrom == null ? LocalDate.now() : startFrom,
                    workShift == null ? WorkShift.DAY : workShift
            ).execute(acc.getAccount());
        } else {
            Toast.makeText(this, R.string.please_sign_in, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    public void showDatePicker(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "date picker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.startFrom = LocalDate.of(year, month + 1, dayOfMonth);
        TextView selectedDate = findViewById(R.id.selected_date);
        Locale locale = LocaleUtils.getLocaleFor(getApplicationContext());
        selectedDate.setText(this.startFrom.format(DateUtils.DATE_TIME_FORMATTER.withLocale(locale)));
    }

    public static final class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private DatePickerDialog.OnDateSetListener callbackListener;

        @Override
        public void onAttach(Context activity) {
            super.onAttach(activity);

            try {
                callbackListener = (DatePickerDialog.OnDateSetListener) activity;

            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnDateSetListener.");
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), callbackListener, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            if (callbackListener != null) {
                callbackListener.onDateSet(view, year, month, dayOfMonth);
            }
        }
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.creation_is_in_progress));
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }

    public void onRecoverableAuthException(UserRecoverableAuthIOException recoverableException) {
        Log.w(TAG, "onRecoverableAuthException", recoverableException);
        startActivityForResult(recoverableException.getIntent(), RC_RECOVERABLE);
    }

}
