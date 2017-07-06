package co.timetableapp.ui.events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import co.timetableapp.R;
import co.timetableapp.TimetableApplication;
import co.timetableapp.data.handler.EventHandler;
import co.timetableapp.model.Event;
import co.timetableapp.model.Timetable;
import co.timetableapp.ui.base.ItemEditActivity;
import co.timetableapp.util.DateUtils;
import co.timetableapp.util.TextUtilsKt;

/**
 * Allows the user to edit an {@link Event}
 *
 * @see ItemEditActivity
 */
public class EventEditActivity extends ItemEditActivity<Event> {

    private EventHandler mDataHandler = new EventHandler(this);

    private EditText mEditTextTitle;
    private EditText mEditTextDetail;

    private LocalDate mEventDate;
    private TextView mDateText;

    private LocalTime mStartTime, mEndTime;
    private TextView mStartTimeText, mEndTimeText;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_event_edit;
    }

    @Override
    protected int getTitleRes(boolean isNewItem) {
        return isNewItem ? R.string.title_activity_event_new : R.string.title_activity_event_edit;
    }

    @Override
    protected void setupLayout() {
        mEditTextTitle = (EditText) findViewById(R.id.editText_title);
        if (!mIsNew) {
            mEditTextTitle.setText(mItem.getTitle());
        }

        mEditTextDetail = (EditText) findViewById(R.id.editText_detail);
        if (!mIsNew) {
            mEditTextDetail.setText(mItem.getNotes());
        }

        setupDateText();
        setupStartTimeText();
        setupEndTimeText();
    }

    private void setupDateText() {
        mDateText = (TextView) findViewById(R.id.textView_date);

        if (!mIsNew) {
            mEventDate = mItem.getStartDateTime().toLocalDate();
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
        mDateText.setText(mEventDate.format(DateUtils.FORMATTER_FULL_DATE));
        mDateText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
    }

    private void setupStartTimeText() {
        mStartTimeText = (TextView) findViewById(R.id.textView_start_time);

        if (!mIsNew) {
            mStartTime = mItem.getStartDateTime().toLocalTime();
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
            mEndTime = mItem.getEndDateTime().toLocalTime();
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
    protected void handleDoneAction() {
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

        int id = mIsNew ? mDataHandler.getHighestItemId() + 1 : mItem.getId();

        Timetable timetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert timetable != null;

        mItem = new Event(
                id,
                timetable.getId(),
                newTitle,
                newDetail,
                LocalDateTime.of(mEventDate, mStartTime),
                LocalDateTime.of(mEventDate, mEndTime));

        if (mIsNew) {
            mDataHandler.addItem(mItem);
        } else {
            mDataHandler.replaceItem(mItem.getId(), mItem);
        }

        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        supportFinishAfterTransition();
    }

    @Override
    protected void handleDeleteAction() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_exam)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDataHandler.deleteItem(mItem.getId());
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

}
