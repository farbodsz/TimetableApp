package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TextUtilsKt;
import com.satsumasoftware.timetable.db.util.SubjectUtilsKt;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.ColorsAdapter;

import java.util.ArrayList;

public class SubjectEditActivity extends AppCompatActivity {

    protected static final String EXTRA_SUBJECT = "extra_subject";

    private Subject mSubject;
    private boolean mIsNewSubject;

    private EditText mEditText;

    private AlertDialog mColorDialog;
    private Color mColor;

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

        final ImageView imageView = (ImageView) findViewById(R.id.imageView);

        mColor = new Color(mIsNewSubject ? 6 : mSubject.getColorId());
        imageView.setImageResource(mColor.getPrimaryColorResId(this));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SubjectEditActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View customView = inflater.inflate(R.layout.dialog_colors, null);

                final ArrayList<Color> colors = ColorsAdapter.getAllColors();

                ColorsAdapter adapter = new ColorsAdapter(getBaseContext(), colors);
                adapter.setOnEntryClickListener(new ColorsAdapter.OnEntryClickListener() {
                    @Override
                    public void onEntryClick(View view, int position) {
                        mColor = colors.get(position);
                        imageView.setImageResource(mColor.getPrimaryColorResId(getBaseContext()));
                        mColorDialog.dismiss();
                    }
                });

                RecyclerView recyclerView = (RecyclerView) customView.findViewById(R.id.recyclerView);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(SubjectEditActivity.this, 4));
                recyclerView.setAdapter(adapter);

                builder.setView(customView);

                mColorDialog = builder.create();
                mColorDialog.show();
            }
        });
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
            mSubject = new Subject(SubjectUtilsKt.getHighestSubjectId(this) + 1, newName, mColor.getId());
            SubjectUtilsKt.addSubject(this, mSubject);

        } else {
            mSubject.setName(newName);
            mSubject.setColorId(mColor.getId());
            SubjectUtilsKt.replaceSubject(this, mSubject.getId(), mSubject);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SUBJECT, mSubject);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void handleDeleteAction() {
        SubjectUtilsKt.completelyDeleteSubject(this, mSubject.getId());
        setResult(Activity.RESULT_OK);
        finish();
    }

}
