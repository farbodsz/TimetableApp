package com.satsumasoftware.timetable.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.satsumasoftware.timetable.framework.Event;
import com.satsumasoftware.timetable.framework.Exam;

/**
 * Invoked and displayed to the user for editing the details of an exam.
 *
 * Currently, it is also responsible for showing the details, since there is no activity dedicated
 * to merely displaying the details (like in {@link AssignmentDetailActivity}).
 *
 * It can also be called to create a new exam. If so, there will be no intent extra data supplied
 * to this activity (i.e. {@link #EXTRA_EVENT} will be null).
 *
 * @see Exam
 * @see ExamsActivity
 */
public class EventEditActivity extends AppCompatActivity {

    /**
     * The key for the {@link Event} passed through an intent extra.
     *
     * It should be null if we're creating a new exam.
     */
    static final String EXTRA_EVENT = "extra_event";

    private Event mExam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO
    }
}
