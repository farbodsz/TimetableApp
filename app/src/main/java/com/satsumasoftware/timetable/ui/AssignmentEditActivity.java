package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TextUtilsKt;
import com.satsumasoftware.timetable.db.ClassesSchema;
import com.satsumasoftware.timetable.db.TimetableDbHelper;
import com.satsumasoftware.timetable.db.util.AssignmentUtilsKt;
import com.satsumasoftware.timetable.db.util.ClassUtilsKt;
import com.satsumasoftware.timetable.db.util.SubjectUtilsKt;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.ui.adapter.ClassesAdapter;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;

public class AssignmentEditActivity extends AppCompatActivity {

    protected static final String EXTRA_ASSIGNMENT = "extra_assignment";

    private Assignment mAssignment;
    private boolean mIsNew;

    private EditText mEditTextTitle;
    private EditText mEditTextDetail;

    private Class mClass;
    private TextView mClassText;
    private AlertDialog mClassDialog;

    private TextView mDateText;
    private LocalDate mDueDate;

    private int mCompletionProgess = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAssignment = extras.getParcelable(EXTRA_ASSIGNMENT);
        }
        mIsNew = mAssignment == null;

        int titleResId = mIsNew ? R.string.title_activity_assignment_new :
                R.string.title_activity_assignment_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCloseAction();
            }
        });

        mEditTextTitle = (EditText) findViewById(R.id.editText_title);
        if (!mIsNew) {
            mEditTextTitle.setText(mAssignment.getTitle());
        }

        mEditTextDetail = (EditText) findViewById(R.id.editText_detail);
        if (!mIsNew) {
            mEditTextDetail.setText(mAssignment.getDetail());
        }

        mClassText = (TextView) findViewById(R.id.textView_class);
        if (!mIsNew) {
            TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(this);
            Cursor cursor = dbHelper.getReadableDatabase().query(
                    ClassesSchema.TABLE_NAME,
                    null,
                    ClassesSchema._ID + "=?",
                    new String[] {String.valueOf(mAssignment.getClassId())},
                    null, null, null);
            cursor.moveToFirst();
            mClass = new Class(this, cursor);
            cursor.close();

            updateClassText();
        }
        mClassText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentEditActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View customView = inflater.inflate(R.layout.dialog_classes, null);

                final ArrayList<Class> classes = ClassUtilsKt.getClasses(getBaseContext());

                ClassesAdapter adapter = new ClassesAdapter(getBaseContext(), classes);
                adapter.setOnEntryClickListener(new ClassesAdapter.OnEntryClickListener() {
                    @Override
                    public void onEntryClick(View view, int position) {
                        mClass = classes.get(position);
                        updateClassText();
                        mClassDialog.dismiss();
                    }
                });

                RecyclerView recyclerView = (RecyclerView) customView.findViewById(R.id.recyclerView);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(AssignmentEditActivity.this));
                recyclerView.setAdapter(adapter);

                builder.setView(customView);

                mClassDialog = builder.create();
                mClassDialog.show();
            }
        });

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

    private void updateClassText() {
        mClassText.setText(SubjectUtilsKt.getSubjectFromId(
                getBaseContext(), mClass.getSubjectId()).getName());
        mClassText.setTextColor(ContextCompat.getColor(
                getBaseContext(), R.color.mdu_text_black));
    }


    private void updateDateText() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu");

        mDateText.setText(mDueDate.format(formatter));
        mDateText.setTextColor(ContextCompat.getColor(
                getBaseContext(), R.color.mdu_text_black));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
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

        int id = mIsNew ? AssignmentUtilsKt.getHighestAssignmentId(this) + 1 : mAssignment.getId();

        mAssignment = new Assignment(
                id,
                mClass.getId(),
                newTitle,
                mEditTextDetail.getText().toString(),
                mDueDate,
                mCompletionProgess);

        if (mIsNew) {
            AssignmentUtilsKt.addAssignment(this, mAssignment);
        } else {
            AssignmentUtilsKt.replaceAssignment(this, mAssignment.getId(), mAssignment);
        }

        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void handleDeleteAction() {
        AssignmentUtilsKt.deleteAssignment(this, mAssignment.getId());
        setResult(Activity.RESULT_OK);
        finish();
    }

}
