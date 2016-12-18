package com.satsumasoftware.timetable.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.ui.login.SignInActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SplashActivity";

    private static final int SLEEP_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    GoogleSignInAccount account =
                            ((TimetableApplication) getApplication()).getSignInAccount();

                    Intent intent;
                    //if (account == null) {
                    if (true) {
                        Log.d(LOG_TAG, "No account found - proceeding to SignInActivity");
                        intent = new Intent(getBaseContext(), SignInActivity.class);
                        startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Account found - proceeding to MainActivity");
                        intent = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        timer.start();
    }
}
