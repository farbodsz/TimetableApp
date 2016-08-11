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
import android.widget.Button;
import android.widget.EditText;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TextUtilsKt;
import com.satsumasoftware.timetable.db.SubjectsUtils;
import com.satsumasoftware.timetable.framework.Subject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SubjectDetailActivity extends AppCompatActivity {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_NEW, ACTION_EDIT, ACTION_DELETE})
    public @interface Action {}
    public static final int ACTION_NEW = 0;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_DELETE = 2;

    protected static final String EXTRA_SUBJECT = "extra_subject";
    protected static final String EXTRA_LIST_POS = "extra_list_position";
    protected static final String EXTRA_RESULT_ACTION = "extra_result_action";

    private boolean mIsNewSubject;

    private Subject mSubject;
    private int mListPosition = SubjectsActivity.LIST_POS_INVALID;

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
            mListPosition = extras.getInt(EXTRA_LIST_POS);
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

        Button button = (Button) findViewById(R.id.button);
        if (mIsNewSubject) {
            button.setVisibility(View.GONE);
        } else {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleDeleteAction();
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subject_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                handleDoneAction();
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

        @Action int actionType;

        if (mIsNewSubject) {
            mSubject = new Subject(SubjectsUtils.getHighestSubjectId(this) + 1, newName);
            actionType = ACTION_NEW;
        } else {
            mSubject.setName(newName);
            actionType = ACTION_EDIT;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SUBJECT, mSubject);
        intent.putExtra(EXTRA_LIST_POS, mListPosition);
        intent.putExtra(EXTRA_RESULT_ACTION, actionType);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void handleDeleteAction() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SUBJECT, mSubject);
        intent.putExtra(EXTRA_LIST_POS, mListPosition);
        intent.putExtra(EXTRA_RESULT_ACTION, ACTION_DELETE);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}
