package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TextUtilsKt;
import com.satsumasoftware.timetable.db.SubjectsUtils;
import com.satsumasoftware.timetable.framework.Subject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SubjectDetailActivity extends AppCompatActivity {

    protected static final String EXTRA_SUBJECT = "extra_subject";

    private Subject mSubject;
    private boolean mIsNewSubject;

    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSubject = extras.getParcelable(EXTRA_SUBJECT);
        }
        mIsNewSubject = mSubject == null;

        int titleResId = mIsNewSubject ? R.string.title_activity_subject_new :
                R.string.title_activity_subject_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCloseAction();
            }
        });

        mEditText = (EditText) findViewById(R.id.editText);
        if (!mIsNewSubject) {
            mEditText.setText(mSubject.getName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mIsNewSubject) {
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
        String newName = mEditText.getText().toString();
        if (newName.length() == 0) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_invalid_name,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        newName = TextUtilsKt.title(newName);

        if (mIsNewSubject) {
            mSubject = new Subject(SubjectsUtils.getHighestSubjectId(this) + 1, newName);
            SubjectsUtils.addSubject(this, mSubject);

        } else {
            mSubject.setName(newName);
            SubjectsUtils.replaceSubject(this, mSubject.getId(), mSubject);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SUBJECT, mSubject);
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void handleDeleteAction() {
        SubjectsUtils.deleteSubject(this, mSubject.getId());
        setResult(Activity.RESULT_OK);
        finish();
    }

}
