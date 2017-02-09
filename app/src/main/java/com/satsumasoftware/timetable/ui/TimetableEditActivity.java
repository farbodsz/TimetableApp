package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.satsumasoftware.timetable.db.ClassTimeUtils;
import com.satsumasoftware.timetable.db.TermUtils;
import com.satsumasoftware.timetable.db.TimetableDbHelper;
import com.satsumasoftware.timetable.db.TimetableUtils;
import com.satsumasoftware.timetable.db.query.Filters;
import com.satsumasoftware.timetable.db.query.Query;
import com.satsumasoftware.timetable.db.schema.ClassTimesSchema;
import com.satsumasoftware.timetable.db.schema.TermsSchema;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Term;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.TermsAdapter;
import com.satsumasoftware.timetable.util.TextUtilsKt;
import com.satsumasoftware.timetable.util.UiUtils;
import com.satsuware.usefulviews.LabelledSpinner;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Invoked and displayed to the user to edit the details of a timetable.
 *
 * Currently, it is also responsible for showing the details, since there is no activity dedicated
 * to merely displaying the details (like in {@link AssignmentDetailActivity}).
 *
 * It can also be called to create a new timetable. If so, there will be no intent extra data
 * supplied to this activity (i.e. {@link #EXTRA_TIMETABLE} will be null).
 *
 * @see Timetable
 * @see TimetablesActivity
 */
public class TimetableEditActivity extends AppCompatActivity
        implements LabelledSpinner.OnItemChosenListener {

    /**
     * The key for the {@link Timetable} passed through an intent extra.
     *
     * It should be null if we're creating a new timetable.
     */
    static final String EXTRA_TIMETABLE = "extra_timetable";

    private static final int REQUEST_CODE_TERM_EDIT = 1;

    private Timetable mTimetable;

    private boolean mIsFirst;
    private boolean mIsNew;

    private EditText mEditTextName;

    private LocalDate mStartDate, mEndDate;
    private TextView mStartDateText, mEndDateText;

    private int mWeekRotations;
    private LabelledSpinner mSpinnerScheduling, mSpinnerWeekRotations;

    private ArrayList<Term> mTerms;
    private TermsAdapter mAdapter;

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

        setupLayout();
    }

    private void setupLayout() {
        mEditTextName = (EditText) findViewById(R.id.editText_name);
        if (!mIsNew) {
            mEditTextName.setText(mTimetable.getName());
        }

        setupDateTexts();

        mSpinnerScheduling = (LabelledSpinner) findViewById(R.id.spinner_scheduling_type);
        mSpinnerWeekRotations = (LabelledSpinner) findViewById(R.id.spinner_scheduling_detail);

        mSpinnerScheduling.setOnItemChosenListener(this);
        mSpinnerWeekRotations.setOnItemChosenListener(this);

        mWeekRotations = mIsNew ? 1 : mTimetable.getWeekRotations();
        updateSchedulingSpinners();

        setupTermsList();

        setupAddTermButton();
    }

    private void setupDateTexts() {
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

    private void setupTermsList() {
        mTerms = getTermsForTimetable(findTimetableId());
        sortList();

        mAdapter = new TermsAdapter(mTerms);
        mAdapter.setOnEntryClickListener(new TermsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(TimetableEditActivity.this, TermEditActivity.class);
                intent.putExtra(TermEditActivity.EXTRA_TERM, mTerms.get(position));
                intent.putExtra(TermEditActivity.EXTRA_TIMETABLE_ID, findTimetableId());

                Bundle bundle = null;
                if (UiUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    TimetableEditActivity.this,
                                    view,
                                    getString(R.string.transition_2));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        TimetableEditActivity.this, intent, REQUEST_CODE_TERM_EDIT, bundle);
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
    }

    private void setupAddTermButton() {
        Button btnAddTerm = (Button) findViewById(R.id.button_add_term);
        btnAddTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimetableEditActivity.this, TermEditActivity.class);
                intent.putExtra(TermEditActivity.EXTRA_TIMETABLE_ID, findTimetableId());

                Bundle bundle = null;
                if (UiUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    TimetableEditActivity.this,
                                    view,
                                    getString(R.string.transition_2));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        TimetableEditActivity.this, intent, REQUEST_CODE_TERM_EDIT, bundle);
            }
        });
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

    private void sortList() {
        Collections.sort(mTerms, new Comparator<Term>() {
            @Override
            public int compare(Term t1, Term t2) {
                return t1.getStartDate().compareTo(t2.getStartDate());
            }
        });
    }

    private void refreshList() {
        mTerms.clear();
        mTerms.addAll(getTermsForTimetable(findTimetableId()));
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    private ArrayList<Term> getTermsForTimetable(int timetableId) {
        Query query = new Query.Builder()
                .addFilter(Filters.equal(TermsSchema.COL_TIMETABLE_ID, String.valueOf(timetableId)))
                .build();
        return new TermUtils().getAllItems(this, query);
    }

    private int findTimetableId() {
        return mTimetable == null ?
                new TimetableUtils().getHighestItemId(this) + 1 : mTimetable.getId();
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
    }

    private void handleCloseAction() {
        if (mIsFirst) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_first_timetable_required, Snackbar.LENGTH_SHORT);
            return;
        }
        setResult(Activity.RESULT_CANCELED);
        supportFinishAfterTransition();
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
                    ClassTime classTime = ClassTime.from(cursor);
                    new ClassTimeUtils().deleteItemWithReferences(this, classTime.getId());
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }

        mTimetable = new Timetable(findTimetableId(), name, mStartDate, mEndDate, mWeekRotations);

        if (mIsNew) {
            new TimetableUtils().addItem(this, mTimetable);
        } else {
            new TimetableUtils().replaceItem(this, mTimetable.getId(), mTimetable);
        }

        TimetableApplication application = (TimetableApplication) getApplication();
        application.setCurrentTimetable(this, mTimetable);

        setResult(Activity.RESULT_OK);
        supportFinishAfterTransition();
    }

    private void handleDeleteAction() {
        if (new TimetableUtils().getAllItems(this).size() == 1) {
            // there needs to be at least one timetable for the app to work
            Snackbar.make(findViewById(R.id.rootView), R.string.message_first_timetable_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_timetable)
                .setMessage(R.string.delete_confirmation_timetable)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new TimetableUtils().deleteItemWithReferences(
                                getBaseContext(), mTimetable.getId());

                        // After the timetable has been deleted, change the current timetable
                        int highestId = new TimetableUtils().getHighestItemId(getBaseContext());
                        Timetable newCurrentTimetable =
                                Timetable.create(getBaseContext(), highestId);

                        TimetableApplication application = (TimetableApplication) getApplication();
                        application.setCurrentTimetable(getBaseContext(), newCurrentTimetable);

                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

}
