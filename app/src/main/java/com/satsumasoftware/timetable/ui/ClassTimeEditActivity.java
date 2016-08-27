package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.db.util.ClassUtils;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Timetable;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalTime;

import java.util.ArrayList;

public class ClassTimeEditActivity extends AppCompatActivity {

    protected static final String EXTRA_CLASS_TIME = "extra_class_time";
    protected static final String EXTRA_CLASS_DETAIL_ID = "extra_class_detail_id";
    protected static final String EXTRA_TAB_POSITION = "extra_tab_position";

    private int mTabPos;
    private boolean mIsNewTime;

    private ArrayList<ClassTime> mClassTimes;
    private int mClassDetailId;

    private TextView mStartTimeText, mEndTimeText;
    private LocalTime mStartTime, mEndTime;

    private TextView mDayText;
    private AlertDialog mDayDialog;
    private SparseArray<DayOfWeek> mDaysOfWeek;

    private TextView mWeekText;
    private AlertDialog mWeekDialog;
    private SparseArray<Integer> mWeekNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_time_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        mClassDetailId = extras.getInt(EXTRA_CLASS_DETAIL_ID);
        mTabPos = extras.getInt(EXTRA_TAB_POSITION);

        if (extras.getParcelableArrayList(EXTRA_CLASS_TIME) != null) {
            mClassTimes = extras.getParcelableArrayList(EXTRA_CLASS_TIME);
        }
        mIsNewTime = mClassTimes == null;

        // validation
        if (!mIsNewTime) {
            LocalTime startTime = null;
            LocalTime endTime = null;
            for (ClassTime classTime : mClassTimes) {
                if (startTime == null) {
                    // set values in first iteration
                    startTime = classTime.getStartTime();
                    endTime = classTime.getEndTime();
                    continue;
                }

                if (!classTime.getStartTime().equals(startTime) ||
                        !classTime.getEndTime().equals(endTime)) {
                    throw new IllegalArgumentException("invalid time - all start and end times " +
                            "must be the same");
                }
            }
        }

        mDaysOfWeek = new SparseArray<>();
        mWeekNumbers = new SparseArray<>();

        int titleResId = mIsNewTime ? R.string.title_activity_class_time_new :
                R.string.title_activity_class_time_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCloseAction();
            }
        });

        mDayText = (TextView) findViewById(R.id.textView_day);
        if (!mIsNewTime) {
            for (ClassTime classTime : mClassTimes) {
                DayOfWeek dayOfWeek = classTime.getDay();
                mDaysOfWeek.put(dayOfWeek.getValue() - 1, dayOfWeek);
            }
            updateDayText();
        }
        mDayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ClassTimeEditActivity.this);

                boolean[] checkedItems = new boolean[7];
                for (int i = 0; i < 7; i++) {
                    checkedItems[i] = mDaysOfWeek.get(i) != null;
                }

                builder.setTitle(R.string.property_day)
                        .setMultiChoiceItems(R.array.days, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    mDaysOfWeek.put(which, DayOfWeek.of(which + 1));
                                } else {
                                    mDaysOfWeek.remove(which);
                                }
                            }
                        })
                        .setPositiveButton(R.string.action_done, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateDayText();
                                mDayDialog.dismiss();
                            }
                        });

                mDayDialog = builder.create();
                mDayDialog.show();
            }
        });

        Timetable timetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert timetable != null;
        final int weekRotations = timetable.getWeekRotations();

        mWeekText = (TextView) findViewById(R.id.textView_week);

        if (timetable.hasFixedScheduling()) {
            mWeekText.setVisibility(View.GONE);
            mWeekNumbers.put(0, 1);

        } else {
            if (!mIsNewTime) {
                for (ClassTime classTime : mClassTimes) {
                    int weekNumber = classTime.getWeekNumber();
                    mWeekNumbers.put(weekNumber - 1, weekNumber);
                }
                updateWeekText();
            }

            mWeekText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ClassTimeEditActivity.this);

                    ArrayList<String> weekItemsList = new ArrayList<>();
                    for (int i = 1; i <= weekRotations; i++) {
                        String item = getString(R.string.week_item, String.valueOf(i));
                        weekItemsList.add(item);
                    }
                    final String[] weekItems = weekItemsList.toArray(new String[weekItemsList.size()]);

                    boolean[] checkedItems = new boolean[weekRotations];
                    for (int i = 0; i < weekRotations; i++) {
                        checkedItems[i] = mWeekNumbers.get(i) != null;
                    }

                    builder.setTitle(R.string.property_week)
                            .setMultiChoiceItems(weekItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    if (isChecked) {
                                        mWeekNumbers.put(which, which + 1);
                                    } else {
                                        mWeekNumbers.remove(which);
                                    }
                                }
                            })
                            .setPositiveButton(R.string.action_done, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateWeekText();
                                    mWeekDialog.dismiss();
                                }
                            });

                    mWeekDialog = builder.create();
                    mWeekDialog.show();
                }
            });
        }

        mStartTimeText = (TextView) findViewById(R.id.textView_start_time);
        mEndTimeText = (TextView) findViewById(R.id.textView_end_time);

        if (!mIsNewTime) {
            mStartTime = mClassTimes.get(0).getStartTime();
            mEndTime = mClassTimes.get(0).getEndTime();
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

                new TimePickerDialog(ClassTimeEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        mStartTime = LocalTime.of(hour, minute);
                        updateTimeTexts();
                    }
                }, initialHour, initialMinute, true).show();
            }
        });
        mEndTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int initialHour = 9;
                int initialMinute = 0;
                if (mEndTime != null) {
                    initialHour = mEndTime.getHour();
                    initialMinute = mEndTime.getMinute();
                }
                new TimePickerDialog(ClassTimeEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        mEndTime = LocalTime.of(hour, minute);
                        updateTimeTexts();
                    }
                }, initialHour, initialMinute, true).show();
            }
        });
    }

    private void updateDayText() {
        if (mDaysOfWeek.size() == 0) {
            mDayText.setText(R.string.property_day);
            mDayText.setTextColor(ContextCompat.getColor(
                    getBaseContext(), R.color.mdu_text_black_secondary));
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            DayOfWeek dayOfWeek = mDaysOfWeek.get(i);
            if (mDaysOfWeek.get(i) != null) {
                builder.append(dayOfWeek.toString());
                builder.append(", ");
            }
        }

        mDayText.setText(builder.toString());
        mDayText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
    }

    private void updateWeekText() {
        if (mWeekNumbers.size() == 0) {
            mWeekText.setText(R.string.property_week);
            mWeekText.setTextColor(ContextCompat.getColor(
                    getBaseContext(), R.color.mdu_text_black_secondary));
            return;
        }

        Timetable timetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert timetable != null;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < timetable.getWeekRotations(); i++) {
            if (mWeekNumbers.get(i) != null) {
                int weekNumber = mWeekNumbers.get(i);
                builder.append(weekNumber);
                builder.append(", ");
            }
        }

        mWeekText.setText(builder.toString());
        mWeekText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
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
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mIsNewTime) {
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
        super.onBackPressed();
    }

    private void handleCloseAction() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void handleDoneAction() {
        if (mDaysOfWeek.size() == 0) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_days_required, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (mWeekNumbers.size() == 0) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_weeks_required, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mStartTime == null || mEndTime == null) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_times_required, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (mStartTime.equals(mEndTime)) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_start_time_equal_end, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (mStartTime.isAfter(mEndTime)) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_start_time_after_end, Snackbar.LENGTH_SHORT).show();
            return;
        }

        Timetable timetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert timetable != null;

        if (!mIsNewTime) {
            for (ClassTime classTime : mClassTimes) {
                ClassUtils.completelyDeleteClassTime(this, classTime.getId());
            }
        }

        for (int i = 0; i < timetable.getWeekRotations(); i++) {
            if (mWeekNumbers.get(i) == null) {
                continue;
            }
            int weekNumber = mWeekNumbers.get(i);

            for (int j = 0; j < 7; j++) {
                DayOfWeek dayOfWeek = mDaysOfWeek.get(j);
                if (dayOfWeek == null) {
                    continue;
                }

                int id = ClassUtils.getHighestClassTimeId(this) + 1;

                ClassTime classTime = new ClassTime(id, timetable.getId(), mClassDetailId,
                        dayOfWeek, weekNumber, mStartTime, mEndTime);

                // Everything will be added fresh regardless of whether or not it is new.
                // This is because there may be more or less ClassTimes than before so ids cannot
                // be replaced exactly (delete 1, add 1).
                ClassUtils.addClassTime(this, classTime);
            }

            Intent intent = new Intent();
            intent.putExtra(EXTRA_TAB_POSITION, mTabPos);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private void handleDeleteAction() {
        for (ClassTime classTime : mClassTimes) {
            ClassUtils.completelyDeleteClassTime(this, classTime.getId());
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TAB_POSITION, mTabPos);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}
