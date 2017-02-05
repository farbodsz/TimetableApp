package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.db.util.EventUtils;
import com.satsumasoftware.timetable.framework.Event;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.util.TextUtilsKt;
import com.satsumasoftware.timetable.util.UiUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * Invoked and displayed to the user for editing the details of an event.
 *
 * Currently, it is also responsible for showing the details, since there is no activity dedicated
 * to merely displaying the details (like in {@link AssignmentDetailActivity}).
 *
 * It can also be called to create a new event. If so, there will be no intent extra data supplied
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

    private Event mEvent;

    private boolean mIsNew;

    private EditText mEditTextTitle;
    private EditText mEditTextDetail;

    private LocalDate mEventDate;
    private TextView mDateText;

    private LocalTime mStartTime, mEndTime;
    private TextView mStartTimeText, mEndTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mEvent = extras.getParcelable(EXTRA_EVENT);
        }
        mIsNew = mEvent == null;

        int titleResId = mIsNew ? R.string.title_activity_event_new :
                R.string.title_activity_event_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        toolbar.setNavigationIcon(UiUtils.tintDrawable(this, R.drawable.ic_close_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCloseAction();
            }
        });

        setupLayout();
    }

    private void setupLayout() {
        mEditTextTitle = (EditText) findViewById(R.id.editText_title);
        if (!mIsNew) {
            mEditTextTitle.setText(mEvent.getTitle());
        }

        mEditTextDetail = (EditText) findViewById(R.id.editText_detail);
        if (!mIsNew) {
            mEditTextTitle.setText(mEvent.getTitle());
        }

        setupDateText();
        setupStartTimeText();
        setupEndTimeText();
    }

    private void setupDateText() {
        mDateText = (TextView) findViewById(R.id.textView_date);

        if (!mIsNew) {
            mEventDate = mEvent.getStartTime().toLocalDate();
            updateDateText();
        }

        mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // note: -1 and +1s in code because Android month values are from 0-11 (to
                // correspond with java.util.Calendar) but LocalDate month values are from 1-12

                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        mEventDate = LocalDate.of(year, month + 1, dayOfMonth);
                        updateDateText();
                    }
                };

                new DatePickerDialog(
                        EventEditActivity.this,
                        listener,
                        mIsNew ? LocalDate.now().getYear() : mEventDate.getYear(),
                        mIsNew ? LocalDate.now().getMonthValue() - 1 : mEventDate.getMonthValue() - 1,
                        mIsNew ? LocalDate.now().getDayOfMonth() : mEventDate.getDayOfMonth()
                ).show();
            }
        });
    }

    private void updateDateText() {
        mDateText.setText(mEventDate.format(DateTimeFormatter.ofPattern("d MMMM uuuu")));
        mDateText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
    }

    private void setupStartTimeText() {
        mStartTimeText = (TextView) findViewById(R.id.textView_start_time);

        if (!mIsNew) {
            mStartTime = mEvent.getStartTime().toLocalTime();
            updateTimeTexts();
        }

        mStartTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int initialHour = 9;
                int initialMinute = 0;
                if (mStartTime != null) {
                    initialHour = mStartTime.getHour();
                    initialMinute = mStartTime.getMinute();
                }

                new TimePickerDialog(EventEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        mStartTime = LocalTime.of(hour, minute);
                        updateTimeTexts();
                    }
                }, initialHour, initialMinute, true).show();
            }
        });
    }

    private void setupEndTimeText() {
        mEndTimeText = (TextView) findViewById(R.id.textView_end_time);

        if (!mIsNew) {
            mEndTime = mEvent.getEndTime().toLocalTime();
            updateTimeTexts();
        }

        mEndTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int initialHour = 9;
                int initialMinute = 0;
                if (mEndTime != null) {
                    initialHour = mEndTime.getHour();
                    initialMinute = mEndTime.getMinute();
                }

                new TimePickerDialog(EventEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        mEndTime = LocalTime.of(hour, minute);
                        updateTimeTexts();
                    }
                }, initialHour, initialMinute, true).show();
            }
        });
    }

    private void updateTimeTexts() {
        if (mStartTime != null) {
            mStartTimeText.setText(mStartTime.toString());
            mStartTimeText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
        }

        if (mEndTime != null) {
            mEndTimeText.setText(mEndTime.toString());
            mEndTimeText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_edit, menu);
        UiUtils.tintMenuIcons(this, menu, R.id.action_done);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mIsNew) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                handleDoneAction();
                break;
            case R.id.action_delete:
                handleDeleteAction();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        handleCloseAction();
    }

    private void handleCloseAction() {
        setResult(Activity.RESULT_CANCELED);
        supportFinishAfterTransition();
    }

    private void handleDoneAction() {
        String newTitle = mEditTextTitle.getText().toString();
        newTitle = TextUtilsKt.title(newTitle);

        String newDetail = mEditTextDetail.getText().toString().trim();

        if (newTitle.trim().isEmpty()) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_title_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mEventDate == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_date_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mStartTime == null || mEndTime == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_times_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        int id = mIsNew ? EventUtils.getHighestEventId(this) + 1 : mEvent.getId();

        Timetable timetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert timetable != null;

        mEvent = new Event(
                id,
                timetable.getId(),
                newTitle,
                newDetail,
                LocalDateTime.of(mEventDate, mStartTime),
                LocalDateTime.of(mEventDate, mEndTime));

        if (mIsNew) {
            EventUtils.addEvent(this, mEvent);
        } else {
            EventUtils.replaceEvent(this, mEvent.getId(), mEvent);
        }

        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        supportFinishAfterTransition();
    }

    private void handleDeleteAction() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_exam)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventUtils.deleteEvent(getBaseContext(), mEvent.getId());
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

}
