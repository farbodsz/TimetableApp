package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.db.ClassTimesSchema;
import com.satsumasoftware.timetable.db.TimetableDbHelper;
import com.satsumasoftware.timetable.db.util.ClassUtils;
import com.satsumasoftware.timetable.db.util.TermUtils;
import com.satsumasoftware.timetable.db.util.TimetableUtils;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Term;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.TermsAdapter;
import com.satsumasoftware.timetable.util.TextUtilsKt;
import com.satsuware.usefulviews.LabelledSpinner;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TimetableEditActivity extends AppCompatActivity implements LabelledSpinner.OnItemChosenListener {

    protected static final String EXTRA_TIMETABLE = "extra_timetable";

    private static final int REQUEST_CODE_TERM_EDIT = 1;

    private boolean mIsFirst;

    private Timetable mTimetable;
    private boolean mIsNew;

    private EditText mEditTextName;

    private TextView mStartDateText, mEndDateText;
    private LocalDate mStartDate, mEndDate;

    private LabelledSpinner mSpinnerScheduling, mSpinnerWeekRotations;
    private int mWeekRotations;

    private TermsAdapter mAdapter;
    private ArrayList<Term> mTerms;

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

        mSpinnerScheduling = (LabelledSpinner) findViewById(R.id.spinner_scheduling_type);
        mSpinnerWeekRotations = (LabelledSpinner) findViewById(R.id.spinner_scheduling_detail);

        mSpinnerScheduling.setOnItemChosenListener(this);
        mSpinnerWeekRotations.setOnItemChosenListener(this);

        mWeekRotations = mIsNew ? 1 : mTimetable.getWeekRotations();
        updateSchedulingSpinners();

        mTerms = TermUtils.getTerms(this, mTimetable);
        sortList();

        mAdapter = new TermsAdapter(mTerms);
        mAdapter.setOnEntryClickListener(new TermsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                // TODO
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        recyclerView.setAdapter(mAdapter);

        Button btnAddTerm = (Button) findViewById(R.id.button_add_term);
        btnAddTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
                // startActivityForResult
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

    private void updateSchedulingSpinners() {
        if (mWeekRotations == 1) {
            mSpinnerScheduling.setSelection(0);
            mSpinnerWeekRotations.setVisibility(View.GONE);
        } else {
            mSpinnerScheduling.setSelection(1);
            mSpinnerWeekRotations.setVisibility(View.VISIBLE);
            // e.g. weekRotations of 2 will be position 0 as in the string-array
            mSpinnerWeekRotations.setSelection(mWeekRotations - 2);
        }
    }

    @Override
    public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView,
                             int position, long id) {
        switch (labelledSpinner.getId()) {
            case R.id.spinner_scheduling_type:
                boolean isFixedScheduling = position == 0;
                if (isFixedScheduling) {
                    mWeekRotations = 1;
                } else {
                    mWeekRotations = (mIsNew || mTimetable.getWeekRotations() == 1) ?
                            2 : mTimetable.getWeekRotations();
                }
                updateSchedulingSpinners();
                break;
            case R.id.spinner_scheduling_detail:
                if (mWeekRotations != 1) {  // only modify mWeekRotations if not fixed scheduling
                    mWeekRotations = position + 2; // as '2 weeks' is position 0
                }
                updateSchedulingSpinners();
                break;
        }
    }

    private void refreshList() {
        mTerms.clear();
        mTerms.addAll(TermUtils.getTerms(this, mTimetable));
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    private void sortList() {
        Collections.sort(mTerms, new Comparator<Term>() {
            @Override
            public int compare(Term t1, Term t2) {
                return t1.getStartDate().compareTo(t2.getStartDate());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TERM_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList();
            }
        }
    }

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {}

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
        String name = TextUtilsKt.title(mEditTextName.getText().toString());

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

        if (!mIsNew) {
            // delete class times with an invalid week number
            if (mWeekRotations < mTimetable.getWeekRotations()) {
                TimetableDbHelper helper = TimetableDbHelper.getInstance(this);
                Cursor cursor = helper.getReadableDatabase().query(
                        ClassTimesSchema.TABLE_NAME,
                        null,
                        ClassTimesSchema.COL_WEEK_NUMBER + ">?",
                        new String[]{String.valueOf(mWeekRotations)},
                        null, null, null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    ClassTime classTime = new ClassTime(cursor);
                    ClassUtils.completelyDeleteClassTime(this, classTime.getId());
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }

        int id = mIsNew ? TimetableUtils.getHighestTimetableId(this) + 1 : mTimetable.getId();
        mTimetable = new Timetable(id, name, mStartDate, mEndDate, mWeekRotations);

        if (mIsNew) {
            TimetableUtils.addTimetable(this, mTimetable);
        } else {
            TimetableUtils.replaceTimetable(this, mTimetable.getId(), mTimetable);
        }

        TimetableApplication application = (TimetableApplication) getApplication();
        application.setCurrentTimetable(mTimetable);
        application.refreshAlarms(this);

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
        Timetable newCurrentTimetable = Timetable.create(this, highestId);

        TimetableApplication application = (TimetableApplication) getApplication();
        application.setCurrentTimetable(newCurrentTimetable);
        application.refreshAlarms(this);

        setResult(Activity.RESULT_OK);
        finish();
    }
}
