package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.db.util.TimetableUtils;
import com.satsumasoftware.timetable.framework.Timetable;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

public class TimetableEditActivity extends AppCompatActivity {

    protected static final String EXTRA_TIMETABLE = "extra_timetable";

    private boolean mIsFirst;

    private Timetable mTimetable;
    private boolean mIsNew;

    private EditText mEditTextName;
    private TextView mStartDateText, mEndDateText;

    private LocalDate mStartDate, mEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mTimetable = extras.getParcelable(EXTRA_TIMETABLE);
        }
        mIsNew = mTimetable == null;

        mIsFirst = ((TimetableApplication) getApplication()).getCurrentTimetable() == null;

        int titleResId = mIsNew ? R.string.title_activity_timetable_new :
                R.string.title_activity_timetable_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCloseAction();
            }
        });

        mEditTextName = (EditText) findViewById(R.id.editText_name);
        if (!mIsNew) {
            mEditTextName.setText(mTimetable.getName());
        }
        mStartDateText = (TextView) findViewById(R.id.textView_start_date);
        mEndDateText = (TextView) findViewById(R.id.textView_end_date);

        if (!mIsNew) {
            mStartDate = mTimetable.getStartDate();
            mEndDate = mTimetable.getEndDate();
            updateDateTexts();
        }

        mStartDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // note: -1 and +1s in code because Android month values are from 0-11 (to
                // correspond with java.util.Calendar) but LocalDate month values are from 1-12

                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        mStartDate = LocalDate.of(year, month + 1, dayOfMonth);
                        updateDateTexts();
                    }
                };

                new DatePickerDialog(
                        TimetableEditActivity.this,
                        listener,
                        mIsNew ? LocalDate.now().getYear() : mStartDate.getYear(),
                        mIsNew ? LocalDate.now().getMonthValue() - 1 : mStartDate.getMonthValue() - 1,
                        mIsNew ? LocalDate.now().getDayOfMonth() : mStartDate.getDayOfMonth()
                ).show();
            }
        });
        mEndDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        mEndDate = LocalDate.of(year, month + 1, dayOfMonth);
                        updateDateTexts();
                    }
                };

                new DatePickerDialog(
                        TimetableEditActivity.this,
                        listener,
                        mIsNew ? LocalDate.now().getYear() : mEndDate.getYear(),
                        mIsNew ? LocalDate.now().getMonthValue() - 1 : mEndDate.getMonthValue() - 1,
                        mIsNew ? LocalDate.now().getDayOfMonth() : mEndDate.getDayOfMonth()
                ).show();
            }
        });
    }

    private void updateDateTexts() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM uuuu");
        if (mStartDate != null) {
            mStartDateText.setText(mStartDate.format(formatter));
            mStartDateText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
        }
        if (mEndDate != null) {
            mEndDateText.setText(mEndDate.format(formatter));
            mEndDateText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
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
        super.onBackPressed();
    }

    private void handleCloseAction() {
        if (mIsFirst) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_first_timetable_required, Snackbar.LENGTH_SHORT);
            return;
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void handleDoneAction() {
        String name = mEditTextName.getText().toString();

        if (mStartDate == null || mEndDate == null) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_times_required, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (mStartDate.equals(mEndDate)) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_start_time_equal_end, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (mStartDate.isAfter(mEndDate)) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_start_time_after_end, Snackbar.LENGTH_SHORT).show();
            return;
        }

        int id = mIsNew ? TimetableUtils.getHighestTimetableId(this) + 1 : mTimetable.getId();
        mTimetable = new Timetable(id, name, mStartDate, mEndDate);

        if (mIsNew) {
            TimetableUtils.addTimetable(this, mTimetable);
        } else {
            TimetableUtils.replaceTimetable(this, mTimetable.getId(), mTimetable);
        }

        ((TimetableApplication) getApplication()).setCurrentTimetable(mTimetable);

        setResult(Activity.RESULT_OK);
        finish();
    }

    private void handleDeleteAction() {
        if (TimetableUtils.getTimetables(this).size() == 1) {
            // there needs to be at least one timetable for the app to work
            Snackbar.make(findViewById(R.id.rootView), R.string.message_first_timetable_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        TimetableUtils.completelyDeleteTimetable(this, mTimetable.getId());

        int highestId = TimetableUtils.getHighestTimetableId(this);
        Timetable newCurrentTimetable = TimetableUtils.getTimetableWithId(this, highestId);

        ((TimetableApplication) getApplication()).setCurrentTimetable(newCurrentTimetable);

        setResult(Activity.RESULT_OK);
        finish();
    }
}
