package com.satsumasoftware.timetable.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.framework.Timetable;

public class SplashActivity extends AppCompatActivity {

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
                    Timetable currentTimetable =
                            ((TimetableApplication) getApplication()).getCurrentTimetable();

                    Intent intent;
                    if (currentTimetable == null) {
                        intent = new Intent(getBaseContext(), TimetableEditActivity.class);
                    } else {
                        intent = new Intent(getBaseContext(), MainActivity.class);
                    }
                    startActivity(intent);
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
