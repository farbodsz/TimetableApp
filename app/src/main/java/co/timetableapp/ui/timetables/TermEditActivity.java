package co.timetableapp.ui.timetables;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import co.timetableapp.R;
import co.timetableapp.data.handler.TermHandler;
import co.timetableapp.data.handler.TimetableHandler;
import co.timetableapp.model.Term;
import co.timetableapp.model.Timetable;
import co.timetableapp.ui.base.ItemEditActivity;
import co.timetableapp.util.TextUtilsKt;

/**
 * Allows the user to edit a {@link Term}
 *
 * @see ItemEditActivity
 * @see TimetableEditActivity
 */
public class TermEditActivity extends ItemEditActivity<Term> {

    /**
     * The key for the integer identifier of the {@link Timetable} this {@link Term} belongs to.
     *
     * This is passed to this activity since the timetable identifier is a required attribute of a
     * term, so if we modify of create a term, we need this attribute.
     *
     * @see Timetable#getId()
     */
    static final String EXTRA_TIMETABLE_ID = "extra_timetable_id";

    private int mTimetableId;

    private TermHandler mDataHandler = new TermHandler(this);

    private EditText mEditText;

    private LocalDate mStartDate, mEndDate;
    private TextView mStartDateText, mEndDateText;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_term_edit;
    }

    @Override
    protected void handleExtras() {
        // We also need to get the timetable id for this term
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getParcelable(EXTRA_ITEM) != null) {
                mItem = extras.getParcelable(EXTRA_ITEM);
            }

            // TODO: find a better way of doing this
            // If the timetable is new, then it may not be saved to the database. In this case the
            // value passed for the id will be -1. If this is the case, we need to find what the id
            // would be.
            mTimetableId = extras.getInt(EXTRA_TIMETABLE_ID, -1);
            if (mTimetableId == -1) {
                mTimetableId = new TimetableHandler(this).getHighestItemId() + 1;
            }
        }
        mIsNew = mItem == null;
    }

    @Override
    protected int getTitleRes(boolean isNewItem) {
        return isNewItem ? R.string.title_activity_term_new : R.string.title_activity_term_edit;
    }

    @Override
    protected void setupLayout() {
        mEditText = (EditText) findViewById(R.id.editText);
        if (!mIsNew) {
            mEditText.setText(mItem.getName());
        }

        setupDateTexts();
    }

    private void setupDateTexts() {
        mStartDateText = (TextView) findViewById(R.id.textView_start_date);
        mEndDateText = (TextView) findViewById(R.id.textView_end_date);

        if (!mIsNew) {
            mStartDate = mItem.getStartDate();
            mEndDate = mItem.getEndDate();
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
    protected void handleDoneAction() {
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

        int id = mIsNew ? mDataHandler.getHighestItemId() + 1 : mItem.getId();
        mItem = new Term(id, mTimetableId, newName, mStartDate, mEndDate);

        if (mIsNew) {
            mDataHandler.addItem(mItem);
        } else {
            mDataHandler.replaceItem(mItem.getId(), mItem);
        }

        setResult(Activity.RESULT_OK);
        supportFinishAfterTransition();
    }

    @Override
    protected void handleDeleteAction() {
        mDataHandler.deleteItem(mItem.getId());
        setResult(Activity.RESULT_OK);
        finish();
    }

}
