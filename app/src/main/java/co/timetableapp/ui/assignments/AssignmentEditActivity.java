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

package co.timetableapp.ui.assignments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;

import co.timetableapp.R;
import co.timetableapp.TimetableApplication;
import co.timetableapp.data.handler.AssignmentHandler;
import co.timetableapp.data.handler.ClassHandler;
import co.timetableapp.data.handler.DataNotFoundException;
import co.timetableapp.model.Assignment;
import co.timetableapp.model.Class;
import co.timetableapp.model.Color;
import co.timetableapp.model.Subject;
import co.timetableapp.model.Timetable;
import co.timetableapp.ui.classes.ClassesAdapter;
import co.timetableapp.util.DateUtils;
import co.timetableapp.util.TextUtilsKt;
import co.timetableapp.util.UiUtils;

/**
 * Allows the user to edit the details of an {@link Assignment}.
 *
 * It can also be called to create a new assignment. If so, it will be started by
 * {@link AssignmentDetailActivity} and no data will be passed to this activity (i.e.
 * {@link #EXTRA_ASSIGNMENT} will be null).
 *
 * @see AssignmentDetailActivity
 */
public class AssignmentEditActivity extends AppCompatActivity {

    /**
     * The key for the {@link Assignment} passed through an intent extra.
     *
     * It should be null if we're creating a new assignment.
     */
    static final String EXTRA_ASSIGNMENT = "extra_assignment";

    private Assignment mAssignment;
    private boolean mIsNew;

    private AssignmentHandler mAssignmentHandler = new AssignmentHandler(this);

    private Toolbar mToolbar;

    private EditText mEditTextTitle;
    private EditText mEditTextDetail;

    private Class mClass;
    private TextView mClassText;
    private AlertDialog mClassDialog;

    private TextView mDateText;
    private LocalDate mDueDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_edit);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAssignment = extras.getParcelable(EXTRA_ASSIGNMENT);
        }
        mIsNew = mAssignment == null;

        int titleResId = mIsNew ? R.string.title_activity_assignment_new :
                R.string.title_activity_assignment_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        mToolbar.setNavigationIcon(UiUtils.tintDrawable(this, R.drawable.ic_close_black_24dp));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
            mEditTextTitle.setText(mAssignment.getTitle());
        }

        mEditTextDetail = (EditText) findViewById(R.id.editText_detail);
        if (!mIsNew) {
            mEditTextDetail.setText(mAssignment.getDetail());
        }

        setupClassText();

        setupDateText();
    }

    private void setupClassText() {
        mClassText = (TextView) findViewById(R.id.textView_class);

        if (!mIsNew) {
            try {
                mClass = Class.create(this, mAssignment.getClassId());
            } catch (DataNotFoundException e) {
                e.printStackTrace();
            }
            updateLinkedClass();
        }

        mClassText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentEditActivity.this);

                final ArrayList<Class> classes =
                        new ClassHandler(AssignmentEditActivity.this).getItems(getApplication());

                Collections.sort(classes, new Class.NaturalSortComparator(getBaseContext()));

                ClassesAdapter adapter = new ClassesAdapter(getBaseContext(), classes);
                //adapter.setOnItemClickListener( ... ); // TODO
//                mClass = classes.get(position);
//                updateLinkedClass();
//                mClassDialog.dismiss();

                RecyclerView recyclerView = new RecyclerView(getBaseContext());
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(AssignmentEditActivity.this));
                recyclerView.setAdapter(adapter);

                View titleView = getLayoutInflater().inflate(R.layout.dialog_title_with_padding, null);
                ((TextView) titleView.findViewById(R.id.title)).setText(R.string.choose_class);

                builder.setView(recyclerView)
                        .setCustomTitle(titleView);

                mClassDialog = builder.create();
                mClassDialog.show();
            }
        });
    }

    private void setupDateText() {
        mDateText = (TextView) findViewById(R.id.textView_date);

        if (!mIsNew) {
            mDueDate = mAssignment.getDueDate();
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
                        mDueDate = LocalDate.of(year, month + 1, dayOfMonth);
                        updateDateText();
                    }
                };

                new DatePickerDialog(
                        AssignmentEditActivity.this,
                        listener,
                        mIsNew ? LocalDate.now().getYear() : mDueDate.getYear(),
                        mIsNew ? LocalDate.now().getMonthValue() - 1 : mDueDate.getMonthValue() - 1,
                        mIsNew ? LocalDate.now().getDayOfMonth() : mDueDate.getDayOfMonth()
                ).show();
            }
        });
    }

    private void updateLinkedClass() {
        Subject subject = null;
        try {
            subject = Subject.create(getBaseContext(), mClass.getSubjectId());
        } catch (DataNotFoundException e) {
            e.printStackTrace();
        }
        assert subject != null;

        mClassText.setText(subject.getName());
        mClassText.setTextColor(ContextCompat.getColor(
                getBaseContext(), R.color.mdu_text_black));

        Color color = new Color(subject.getColorId());
        UiUtils.setBarColors(
                color,
                this,
                mToolbar,
                findViewById(R.id.appBarLayout),
                findViewById(R.id.toolbar_container));
    }

    private void updateDateText() {
        mDateText.setText(mDueDate.format(DateUtils.FORMATTER_FULL_DATE));
        mDateText.setTextColor(ContextCompat.getColor(
                getBaseContext(), R.color.mdu_text_black));
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
        finish();
    }

    private void handleDoneAction() {
        String newTitle = mEditTextTitle.getText().toString();
        if (newTitle.length() == 0) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_title_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        newTitle = TextUtilsKt.title(newTitle);

        if (mClass == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_class_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mDueDate == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_due_date_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        int id = mIsNew ? mAssignmentHandler.getHighestItemId() + 1 : mAssignment.getId();
        int completionProgress = mIsNew ? 0 : mAssignment.getCompletionProgress();

        Timetable timetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert timetable != null;

        mAssignment = new Assignment(
                id,
                timetable.getId(),
                mClass.getId(),
                newTitle,
                mEditTextDetail.getText().toString(),
                mDueDate,
                completionProgress);

        if (mIsNew) {
            mAssignmentHandler.addItem(mAssignment);
        } else {
            mAssignmentHandler.replaceItem(mAssignment.getId(), mAssignment);
        }

        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void handleDeleteAction() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_assignment)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAssignmentHandler.deleteItem(mAssignment.getId());
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

}
