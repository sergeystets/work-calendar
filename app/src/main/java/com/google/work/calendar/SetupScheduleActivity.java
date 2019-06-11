package com.google.work.calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.work.calendar.async.BuildScheduleTask;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Calendar;

import static com.google.work.calendar.MainActivity.USER_DISPLAY_NAME;
import static com.google.work.calendar.utils.Status.RC_RECOVERABLE;


public class SetupScheduleActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "SetupScheduleActivity";
    private static final String CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";

    private GoogleSignInClient signInClient;

    private ProgressDialog progressDialog;

    private int brigadeNumber = 1;
    private LocalDate startFrom;

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

    private void signOut() {
        signInClient.signOut().addOnCompleteListener(task -> finish());
    }

    private void buildSchedule() {
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (acc != null) {
            showProgressDialog();
            new BuildScheduleTask(this, brigadeNumber, startFrom == null ? LocalDate.now() : startFrom).execute(acc.getAccount());
        } else {
            Toast.makeText(this, R.string.please_sign_in, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void showDatePicker(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "date picker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.startFrom = LocalDate.of(year, month + 1, dayOfMonth);
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
            progressDialog.setMessage(getString(R.string.creating_message));
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
