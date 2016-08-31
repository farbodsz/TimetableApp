package com.satsumasoftware.timetable.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.ThemeUtils;
import com.satsumasoftware.timetable.db.util.AssignmentUtils;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import org.threeten.bp.format.DateTimeFormatter;

public class AssignmentDetailActivity extends AppCompatActivity {

    protected static final String EXTRA_ASSIGNMENT = "extra_assignment";

    protected static final int REQUEST_CODE_ASSIGNMENT_EDIT = 1;

    private Assignment mAssignment;
    private boolean mIsNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_detail);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            mIsNew = true;
            Intent intent = new Intent(AssignmentDetailActivity.this, AssignmentEditActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_EDIT);
            return;
        }

        mIsNew = false;
        mAssignment = extras.getParcelable(EXTRA_ASSIGNMENT);

        setupLayouts();
    }

    private void setupLayouts() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(ThemeUtils.tintDrawable(this, R.drawable.ic_close_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEditsAndClose();
            }
        });

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(mAssignment.getTitle());

        Class cls = Class.create(this, mAssignment.getClassId());
        assert cls != null;
        Subject subject = Subject.create(this, cls.getSubjectId());
        assert subject != null;
        getSupportActionBar().setSubtitle(subject.getName());

        Color color = new Color(subject.getColorId());
        ThemeUtils.setBarColors(color, this, toolbar);

        TextView dateText = (TextView) findViewById(R.id.textView_date);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu");
        dateText.setText(mAssignment.getDueDate().format(formatter));

        final TextView progressText = (TextView) findViewById(R.id.textView_progress);
        progressText.setText(getString(R.string.property_progress,
                mAssignment.getCompletionProgress()));

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(20); // so it goes up in fives
        seekBar.setProgress(mAssignment.getCompletionProgress() / 5);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAssignment.setCompletionProgress(progress * 5);
                progressText.setText(getString(R.string.property_progress,
                        mAssignment.getCompletionProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        TextView detailText = (TextView) findViewById(R.id.textView_detail);
        if (mAssignment.hasDetail()) {
            detailText.setText(mAssignment.getDetail());
            detailText.setTextColor(ContextCompat.getColor(this, R.color.mdu_text_black));
        } else {
            detailText.setText(Html.fromHtml(getString(R.string.placeholder_detail_empty)));
            detailText.setTextColor(ContextCompat.getColor(this, R.color.mdu_text_black_secondary));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ASSIGNMENT_EDIT) {
            if (resultCode == RESULT_OK) {
                // get the edited assignment (it would have the highest id if new, same id if not)
                mAssignment = Assignment.create(this, mIsNew ?
                        AssignmentUtils.getHighestAssignmentId(this) : mAssignment.getId());

                if (mAssignment == null) {
                    Log.d("ADA!!", "Assignment is NULL");
                    // must have been deleted
                    saveDeleteAndClose();
                    return;
                }

                if (mIsNew) {
                    saveEditsAndClose();
                } else {
                    setupLayouts();
                }

            } else if (resultCode == RESULT_CANCELED) {
                if (mIsNew) {
                    cancelAndClose();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
        ThemeUtils.tintMenuIcons(this, menu, R.id.action_edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent(AssignmentDetailActivity.this, AssignmentEditActivity.class);
                intent.putExtra(AssignmentEditActivity.EXTRA_ASSIGNMENT, mAssignment);
                startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_EDIT);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveEditsAndClose();
        super.onBackPressed();
    }

    private void cancelAndClose() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void saveEditsAndClose() {
        // overwrite db values as completionProgress may have changed
        AssignmentUtils.replaceAssignment(this, mAssignment.getId(), mAssignment);

        setResult(RESULT_OK); // to reload any changes in AssignmentsActivity
        finish();
    }

    private void saveDeleteAndClose() {
        setResult(RESULT_OK); // to reload any changes in AssignmentsActivity
        finish();
    }

}
