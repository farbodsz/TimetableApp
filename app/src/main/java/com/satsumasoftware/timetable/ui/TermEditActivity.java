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
import com.satsumasoftware.timetable.db.TermUtils;
import com.satsumasoftware.timetable.db.TimetableUtils;
import com.satsumasoftware.timetable.framework.Term;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.util.TextUtilsKt;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

/**
 * Invoked and displayed to the user to edit the details of a term, or to create a new one.
 *
 * @see Term
 */
public class TermEditActivity extends AppCompatActivity {

    /**
     * The key for the {@link Term} passed through an intent extra.
     *
     * It should be null if we're creating a new term.
     */
    static final String EXTRA_TERM = "extra_term";

    /**
     * The key for the integer identifier of the {@link Timetable} this {@link Term} belongs to.
     *
     * This is passed to this activity since the timetable identifier is a required attribute of a
     * term, so if we modify of create a term, we need this attribute.
     *
     * @see Timetable#getId()
     */
    static final String EXTRA_TIMETABLE_ID = "extra_timetable_id";

    private Term mTerm;
    private int mTimetableId;

    private boolean mIsNew;

    private TermUtils mTermUtils = new TermUtils(this);

    private EditText mEditText;

    private LocalDate mStartDate, mEndDate;
    private TextView mStartDateText, mEndDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_edit);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getParcelable(EXTRA_TERM) != null) {
                mTerm = extras.getParcelable(EXTRA_TERM);
            }

            mTimetableId = extras.getInt(EXTRA_TIMETABLE_ID, -1);
            if (mTimetableId == -1) {
                mTimetableId = new TimetableUtils(this).getHighestItemId() + 1;
            }
        }
        mIsNew = mTerm == null;

        int titleResId = mIsNew ? R.string.title_activity_term_new :
                R.string.title_activity_term_edit;
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
        mEditText = (EditText) findViewById(R.id.editText);
        if (!mIsNew) {
            mEditText.setText(mTerm.getName());
        }

        setupDateTexts();
    }

    private void setupDateTexts() {
        mStartDateText = (TextView) findViewById(R.id.textView_start_date);
        mEndDateText = (TextView) findViewById(R.id.textView_end_date);

        if (!mIsNew) {
            mStartDate = mTerm.getStartDate();
            mEndDate = mTerm.getEndDate();
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
                        TermEditActivity.this,
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
                        TermEditActivity.this,
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
    }

    private void handleCloseAction() {
        setResult(Activity.RESULT_CANCELED);
        supportFinishAfterTransition();
    }

    private void handleDoneAction() {
        String newName = mEditText.getText().toString();
        if (newName.length() == 0) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_invalid_name,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        newName = TextUtilsKt.title(newName);

        if (mStartDate == null || mEndDate == null) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_term_dates_required, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (mStartDate.equals(mEndDate)) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_start_date_equal_end_date, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (mStartDate.isAfter(mEndDate)) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_start_date_after_end_date, Snackbar.LENGTH_SHORT).show();
            return;
        }

        int id = mIsNew ? mTermUtils.getHighestItemId() + 1 : mTerm.getId();
        mTerm = new Term(id, mTimetableId, newName, mStartDate, mEndDate);

        if (mIsNew) {
            mTermUtils.addItem(mTerm);
        } else {
            mTermUtils.replaceItem(mTerm.getId(), mTerm);
        }

        setResult(Activity.RESULT_OK);
        supportFinishAfterTransition();
    }

    private void handleDeleteAction() {
        mTermUtils.deleteItem(mTerm.getId());
        setResult(Activity.RESULT_OK);
        finish();
    }

}
