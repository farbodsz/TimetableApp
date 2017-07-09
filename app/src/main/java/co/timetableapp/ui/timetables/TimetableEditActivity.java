/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.ui.timetables;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.satsuware.usefulviews.LabelledSpinner;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;

import co.timetableapp.R;
import co.timetableapp.TimetableApplication;
import co.timetableapp.data.TimetableDbHelper;
import co.timetableapp.data.handler.ClassTimeHandler;
import co.timetableapp.data.handler.TermHandler;
import co.timetableapp.data.handler.TimetableHandler;
import co.timetableapp.data.query.Filters;
import co.timetableapp.data.query.Query;
import co.timetableapp.data.schema.ClassTimesSchema;
import co.timetableapp.data.schema.TermsSchema;
import co.timetableapp.model.ClassTime;
import co.timetableapp.model.Term;
import co.timetableapp.model.Timetable;
import co.timetableapp.ui.base.ItemEditActivity;
import co.timetableapp.util.DateUtils;
import co.timetableapp.util.TextUtilsKt;
import co.timetableapp.util.UiUtils;

/**
 * Allows the user to edit a {@link Timetable}
 *
 * @see ItemEditActivity
 * @see TimetablesActivity
 */
public class TimetableEditActivity extends ItemEditActivity<Timetable>
        implements LabelledSpinner.OnItemChosenListener {

    private static final int REQUEST_CODE_TERM_EDIT = 1;

    private boolean mIsFirst;

    private TimetableHandler mDataHandler = new TimetableHandler(this);

    private EditText mEditTextName;

    private LocalDate mStartDate, mEndDate;
    private TextView mStartDateText, mEndDateText;

    private int mWeekRotations;
    private LabelledSpinner mSpinnerScheduling, mSpinnerWeekRotations;

    private ArrayList<Term> mTerms;
    private TermsAdapter mAdapter;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_timetable_edit;
    }

    @Override
    protected void handleExtras() {
        super.handleExtras();
        mIsFirst = ((TimetableApplication) getApplication()).getCurrentTimetable() == null;
    }

    @Override
    protected int getTitleRes(boolean isNewItem) {
        return isNewItem ? R.string.title_activity_timetable_new :
                R.string.title_activity_timetable_edit;
    }

    @Override
    protected void setupLayout() {
        mEditTextName = (EditText) findViewById(R.id.editText_name);
        if (!mIsNew) {
            mEditTextName.setText(mItem.getName());
        }

        setupDateTexts();

        mSpinnerScheduling = (LabelledSpinner) findViewById(R.id.spinner_scheduling_type);
        mSpinnerWeekRotations = (LabelledSpinner) findViewById(R.id.spinner_scheduling_detail);

        mSpinnerScheduling.setOnItemChosenListener(this);
        mSpinnerWeekRotations.setOnItemChosenListener(this);

        mWeekRotations = mIsNew ? 1 : mItem.getWeekRotations();
        updateSchedulingSpinners();

        setupTermsList();

        setupAddTermButton();
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
        if (mStartDate != null) {
            mStartDateText.setText(mStartDate.format(DateUtils.FORMATTER_FULL_DATE));
            mStartDateText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
        }
        if (mEndDate != null) {
            mEndDateText.setText(mEndDate.format(DateUtils.FORMATTER_FULL_DATE));
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
                intent.putExtra(ItemEditActivity.EXTRA_ITEM, mTerms.get(position));
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
                    mWeekRotations = (mIsNew || mItem.getWeekRotations() == 1) ?
                            2 : mItem.getWeekRotations();
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

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {
    }

    private void sortList() {
        Collections.sort(mTerms);
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
        return new TermHandler(this).getAllItems(query);
    }

    private int findTimetableId() {
        return mItem == null ? mDataHandler.getHighestItemId() + 1 : mItem.getId();
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
    protected void handleCloseAction() {
        if (mIsFirst) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_first_timetable_required, Snackbar.LENGTH_SHORT);
            return;
        }
        setResult(Activity.RESULT_CANCELED);
        supportFinishAfterTransition();
    }

    @Override
    protected void handleDoneAction() {
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
            // Delete class times with an invalid week number
            if (mWeekRotations < mItem.getWeekRotations()) {
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
                    new ClassTimeHandler(this).deleteItemWithReferences(classTime.getId());
                    cursor.moveToNext();
                }
                cursor.close();
            }
        }

        mItem = new Timetable(findTimetableId(), name, mStartDate, mEndDate, mWeekRotations);

        if (mIsNew) {
            mDataHandler.addItem(mItem);
        } else {
            mDataHandler.replaceItem(mItem.getId(), mItem);
        }

        TimetableApplication application = (TimetableApplication) getApplication();
        application.setCurrentTimetable(this, mItem);

        setResult(Activity.RESULT_OK);
        supportFinishAfterTransition();
    }

    @Override
    protected void handleDeleteAction() {
        // There needs to be at least one timetable for the app to work
        if (mDataHandler.getAllItems().size() == 1) {
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
                        mDataHandler.deleteItemWithReferences(mItem.getId());

                        // After the timetable has been deleted, change the current timetable
                        Timetable newCurrentTimetable = mDataHandler.getAllItems().get(0);

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
