package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.ClassesUtils;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsuware.usefulviews.LabelledSpinner;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ClassTimeDetailActivity extends AppCompatActivity {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_NEW, ACTION_EDIT, ACTION_DELETE})
    public @interface Action {}
    public static final int ACTION_NEW = 0;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_DELETE = 2;

    protected static final String EXTRA_CLASS_TIME = "extra_class_time";
    protected static final String EXTRA_CLASS_DETAIL_ID = "extra_class_detail_id";
    protected static final String EXTRA_TAB_POSITION = "extra_tab_position";
    protected static final String EXTRA_LIST_POS = "extra_list_position";
    protected static final String EXTRA_RESULT_ACTION = "extra_result_action";

    private int mTabPos;
    private boolean mIsNewTime;

    private ClassTime mClassTime;
    private int mClassDetailId;

    private int mListPosition = SubjectsActivity.LIST_POS_INVALID;

    private TextView mStartTimeText, mEndTimeText;

    private DayOfWeek mDayOfWeek;
    private LocalTime mStartTime, mEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_time_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        mClassDetailId = extras.getInt(EXTRA_CLASS_DETAIL_ID);
        mTabPos = extras.getInt(EXTRA_TAB_POSITION);

        if (extras.getParcelable(EXTRA_CLASS_TIME) != null) {
            mClassTime = extras.getParcelable(EXTRA_CLASS_TIME);
            mListPosition = extras.getInt(EXTRA_LIST_POS);
        }
        mIsNewTime = mClassTime == null;

        int titleResId = mIsNewTime ? R.string.title_activity_class_time_new :
                R.string.title_activity_class_time_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCloseAction();
            }
        });

        LabelledSpinner spinner = (LabelledSpinner) findViewById(R.id.spinner_day);
        if (!mIsNewTime) {
            spinner.setSelection(mClassTime.getDay().getValue() - 1);
        }
        spinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
                mDayOfWeek = DayOfWeek.of(position + 1);
            }
            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {}
        });

        mStartTimeText = (TextView) findViewById(R.id.textView_start_time);
        mEndTimeText = (TextView) findViewById(R.id.textView_end_time);

        if (!mIsNewTime) {
            mStartTime = mClassTime.getStartTime();
            mEndTime = mClassTime.getEndTime();
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

                new TimePickerDialog(ClassTimeDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
                new TimePickerDialog(ClassTimeDetailActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
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

        int id;
        @Action int actionType;
        if (mIsNewTime) {
            id = ClassesUtils.getHighestClassTimeId(this) + 1;
            actionType = ACTION_NEW;
        } else {
            id = mClassTime.getId();
            actionType = ACTION_EDIT;
        }

        mClassTime = new ClassTime(id, mClassDetailId, mDayOfWeek, mStartTime, mEndTime);

        Intent intent = new Intent();
        intent.putExtra(EXTRA_CLASS_TIME, mClassTime);
        intent.putExtra(EXTRA_TAB_POSITION, mTabPos);
        intent.putExtra(EXTRA_LIST_POS, mListPosition);
        intent.putExtra(EXTRA_RESULT_ACTION, actionType);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void handleDeleteAction() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CLASS_TIME, mClassTime);
        intent.putExtra(EXTRA_TAB_POSITION, mTabPos);
        intent.putExtra(EXTRA_LIST_POS, mListPosition);
        intent.putExtra(EXTRA_RESULT_ACTION, ACTION_DELETE);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}
