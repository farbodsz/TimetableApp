package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.db.util.ExamUtils;
import com.satsumasoftware.timetable.db.util.SubjectUtils;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.SubjectsAdapter;
import com.satsumasoftware.timetable.util.TextUtilsKt;
import com.satsumasoftware.timetable.util.UiUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Invoked and displayed to the user for editing the details of an exam.
 *
 * Currently, it is also responsible for showing the details, since there is no activity dedicated
 * to merely displaying the details (like in {@link AssignmentDetailActivity}).
 *
 * It can also be called to create a new exam. If so, there will be no intent extra data supplied
 * to this activity (i.e. {@link #EXTRA_EXAM} will be null).
 *
 * @see Exam
 */
public class ExamEditActivity extends AppCompatActivity {

    /**
     * The key for the {@link Exam} passed through an intent extra.
     *
     * It should be null if we're creating a new class.
     */
    protected static final String EXTRA_EXAM = "extra_exam";

    protected static final int REQUEST_CODE_SUBJECT_DETAIL = 2;

    private static final int NO_DURATION = -1;

    private Exam mExam;

    private boolean mIsNew;

    private Toolbar mToolbar;

    private EditText mEditTextModule;
    private EditText mEditTextSeat;
    private EditText mEditTextRoom;

    private Subject mSubject;
    private TextView mSubjectText;
    private AlertDialog mSubjectDialog;

    private LocalDate mExamDate;
    private TextView mDateText;

    private LocalTime mExamTime;
    private TextView mTimeText;

    private int mExamDuration = NO_DURATION;
    private TextView mDurationText;
    private AlertDialog mDurationDialog;

    private boolean mExamIsResit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_edit);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mExam = extras.getParcelable(EXTRA_EXAM);
        }
        mIsNew = mExam == null;

        int titleResId = mIsNew ? R.string.title_activity_exam_new :
                R.string.title_activity_exam_edit;
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
        mEditTextModule = (EditText) findViewById(R.id.editText_module);
        if (!mIsNew) {
            mEditTextModule.setText(mExam.getModuleName());
        }

        mEditTextSeat = (EditText) findViewById(R.id.editText_seat);
        if (!mIsNew) {
            mEditTextSeat.setText(mExam.getSeat());
        }

        mEditTextRoom = (EditText) findViewById(R.id.editText_room);
        if (!mIsNew) {
            mEditTextRoom.setText(mExam.getRoom());
        }

        setupSubjectText();

        setupDateText();
        setupTimeText();
        setupDurationText();

        setupResitCheckbox();
    }

    private void setupSubjectText() {
        mSubjectText = (TextView) findViewById(R.id.textView_subject);

        if (!mIsNew) {
            mSubject = Subject.create(this, mExam.getSubjectId());
            updateLinkedSubject();
        }

        mSubjectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ExamEditActivity.this);

                final ArrayList<Subject> subjects = SubjectUtils.getSubjects(ExamEditActivity.this);

                Collections.sort(subjects, new Comparator<Subject>() {
                    @Override
                    public int compare(Subject o1, Subject o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                SubjectsAdapter adapter = new SubjectsAdapter(getBaseContext(), subjects);
                adapter.setOnEntryClickListener(new SubjectsAdapter.OnEntryClickListener() {
                    @Override
                    public void onEntryClick(View view, int position) {
                        mSubject = subjects.get(position);
                        updateLinkedSubject();
                        mSubjectDialog.dismiss();
                    }
                });

                RecyclerView recyclerView = new RecyclerView(getBaseContext());
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(ExamEditActivity.this));
                recyclerView.setAdapter(adapter);

                View titleView =
                        getLayoutInflater().inflate(R.layout.dialog_title_with_padding, null);
                ((TextView) titleView.findViewById(R.id.title)).setText(R.string.choose_subject);

                builder.setView(recyclerView)
                        .setCustomTitle(titleView)
                        .setPositiveButton(R.string.action_new, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(
                                        ExamEditActivity.this, SubjectEditActivity.class);
                                startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
                            }
                        });

                mSubjectDialog = builder.create();
                mSubjectDialog.show();
            }
        });
    }

    private void updateLinkedSubject() {
        mSubjectText.setText(mSubject.getName());
        mSubjectText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));

        Color color = new Color(mSubject.getColorId());
        UiUtils.setBarColors(color, this, mToolbar);
    }

    private void setupDateText() {
        mDateText = (TextView) findViewById(R.id.textView_date);

        if (!mIsNew) {
            mExamDate = mExam.getDate();
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
                        mExamDate = LocalDate.of(year, month + 1, dayOfMonth);
                        updateDateText();
                    }
                };

                new DatePickerDialog(
                        ExamEditActivity.this,
                        listener,
                        mIsNew ? LocalDate.now().getYear() : mExamDate.getYear(),
                        mIsNew ? LocalDate.now().getMonthValue() - 1 : mExamDate.getMonthValue() - 1,
                        mIsNew ? LocalDate.now().getDayOfMonth() : mExamDate.getDayOfMonth()
                ).show();
            }
        });
    }

    private void updateDateText() {
        mDateText.setText(mExamDate.format(DateTimeFormatter.ofPattern("d MMMM uuuu")));
        mDateText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
    }

    private void setupTimeText() {
        mTimeText = (TextView) findViewById(R.id.textView_start_time);

        if (!mIsNew) {
            mExamTime = mExam.getStartTime();
            updateTimeText();
        }

        mTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int initialHour = 9;
                int initialMinute = 0;
                if (mExamTime != null) {
                    initialHour = mExamTime.getHour();
                    initialMinute = mExamTime.getMinute();
                }

                new TimePickerDialog(ExamEditActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        mExamTime = LocalTime.of(hour, minute);
                        updateTimeText();
                    }
                }, initialHour, initialMinute, true).show();
            }
        });
    }

    private void updateTimeText() {
        mTimeText.setText(mExamTime.toString());
        mTimeText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
    }

    private void setupDurationText() {
        mDurationText = (TextView) findViewById(R.id.textView_duration);

        if (!mIsNew) {
            mExamDuration = mExam.getDuration();
            updateDurationText();
        }

        mDurationText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ExamEditActivity.this);

                final NumberPicker numberPicker = new NumberPicker(getBaseContext());
                numberPicker.setMinValue(10);
                numberPicker.setMaxValue(360);
                numberPicker.setValue(mExamDuration == NO_DURATION ? 60 : mExamDuration);

                View titleView =
                        getLayoutInflater().inflate(R.layout.dialog_title_with_padding, null);
                ((TextView) titleView.findViewById(R.id.title)).setText(R.string.property_duration);

                builder.setView(numberPicker)
                        .setCustomTitle(titleView)
                        .setPositiveButton(R.string.action_done, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                mExamDuration = numberPicker.getValue();
                                updateDurationText();

                                mDurationDialog.dismiss();
                            }
                        });

                mDurationDialog = builder.create();
                mDurationDialog.show();
            }
        });
    }

    private void updateDurationText() {
        mDurationText.setText(mExamDuration + " mins");
        mDurationText.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.mdu_text_black));
    }

    private void setupResitCheckbox() {
        mExamIsResit = !mIsNew && mExam.getResit();

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox_resit);
        checkBox.setChecked(mExamIsResit);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mExamIsResit = isChecked;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SUBJECT_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                mSubject = data.getParcelableExtra(SubjectEditActivity.EXTRA_SUBJECT);
                mSubjectDialog.dismiss();
                updateLinkedSubject();
            }
        }
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
        supportFinishAfterTransition();
    }

    private void handleDoneAction() {
        String newModuleName = mEditTextModule.getText().toString();
        newModuleName = TextUtilsKt.title(newModuleName);

        if (mSubject == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_subject_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mExamDate == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_exam_date_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mExamTime == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_exam_time_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (mExamDuration == -1) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_exam_duration_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        int id = mIsNew ? ExamUtils.getHighestExamId(this) + 1 : mExam.getId();

        Timetable timetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert timetable != null;

        mExam = new Exam(
                id,
                timetable.getId(),
                mSubject.getId(),
                newModuleName,
                mExamDate,
                mExamTime,
                mExamDuration,
                mEditTextSeat.getText().toString(),
                mEditTextRoom.getText().toString(),
                mExamIsResit);

        if (mIsNew) {
            ExamUtils.addExam(this, mExam);
        } else {
            ExamUtils.replaceExam(this, mExam.getId(), mExam);
        }

        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        supportFinishAfterTransition();
    }

    private void handleDeleteAction() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_exam)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExamUtils.deleteExam(getBaseContext(), mExam.getId());
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

}
