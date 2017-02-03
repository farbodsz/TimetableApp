package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.framework.Timetable;

/**
 * A splash screen shown to the user briefly before starting {@link MainActivity}.
 */
public class SplashActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_TIMETABLE_EDIT = 1;

    /**
     * The duration in milliseconds that the splash screen should be displayed for
     */
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
                        startActivityForResult(intent, REQUEST_CODE_TIMETABLE_EDIT);
                    } else {
                        intent = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TIMETABLE_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
