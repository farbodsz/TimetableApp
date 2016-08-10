package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.framework.Subject;

public class SubjectDetailActivity extends AppCompatActivity {

    protected static final String EXTRA_SUBJECT = "extra_subject";
    protected static final String EXTRA_LIST_POS = "extra_list_position";

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

        if (mIsNewSubject) {
            mSubject = new Subject(1, newName);  // TODO the id should be the latest id
        } else {
            mSubject.setName(newName);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SUBJECT, mSubject);
        intent.putExtra(EXTRA_LIST_POS, mListPosition);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}
