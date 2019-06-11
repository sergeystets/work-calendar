package com.google.work.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;

import static com.google.work.calendar.utils.Scope.CALENDAR_SCOPE;
import static com.google.work.calendar.utils.Status.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String USER_DISPLAY_NAME = "com.google.work.calendar.user.display.name";
    public static final String APPLICATION_NAME = "Work Calendar App";

    private static final String TAG = "MainActivity";

    private GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(CALENDAR_SCOPE))
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            signIn();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                openSetupScheduleActivityFor(account);
            } catch (ApiException e) {
                Log.w(TAG, "handleSignInResult:error", e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, new Scope(CALENDAR_SCOPE))) {
            openSetupScheduleActivityFor(account);
        }
    }

    private void signIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void openSetupScheduleActivityFor(GoogleSignInAccount account) {
        if (account != null) {
            Intent setupScheduleIntent = new Intent(this, SetupScheduleActivity.class);
            setupScheduleIntent.putExtra(USER_DISPLAY_NAME, account.getDisplayName());
            startActivity(setupScheduleIntent);
        }
    }
}
