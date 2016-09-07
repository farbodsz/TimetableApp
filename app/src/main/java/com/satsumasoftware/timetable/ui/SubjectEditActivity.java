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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.db.util.SubjectUtils;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.ColorsAdapter;
import com.satsumasoftware.timetable.util.TextUtilsKt;
import com.satsumasoftware.timetable.util.ThemeUtils;

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
        setContentView(R.layout.activity_subject_edit);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        toolbar.setNavigationIcon(ThemeUtils.tintDrawable(this, R.drawable.ic_close_black_24dp));
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
        ThemeUtils.setBarColors(mColor, SubjectEditActivity.this, toolbar);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SubjectEditActivity.this);

                final ArrayList<Color> colors = ColorsAdapter.getAllColors();

                ColorsAdapter adapter = new ColorsAdapter(getBaseContext(), colors);
                adapter.setOnEntryClickListener(new ColorsAdapter.OnEntryClickListener() {
                    @Override
                    public void onEntryClick(View view, int position) {
                        mColor = colors.get(position);
                        imageView.setImageResource(mColor.getPrimaryColorResId(getBaseContext()));
                        ThemeUtils.setBarColors(mColor, SubjectEditActivity.this, toolbar);
                        mColorDialog.dismiss();
                    }
                });

                RecyclerView recyclerView = new RecyclerView(getBaseContext());
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(SubjectEditActivity.this,
                        getResources().getInteger(R.integer.subject_color_dialog_columns)));
                recyclerView.setAdapter(adapter);

                View titleView = getLayoutInflater().inflate(R.layout.dialog_title_with_padding, null);
                ((TextView) titleView.findViewById(R.id.title)).setText(R.string.choose_color);

                builder.setView(recyclerView)
                        .setCustomTitle(titleView);

                mColorDialog = builder.create();
                mColorDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_edit, menu);
        ThemeUtils.tintMenuIcons(this, menu, R.id.action_done);
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
            Timetable currentTimetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
            assert currentTimetable != null;

            mSubject = new Subject(SubjectUtils.getHighestSubjectId(this) + 1,
                    currentTimetable.getId(), newName, mColor.getId());
            SubjectUtils.addSubject(this, mSubject);

        } else {
            mSubject.setName(newName);
            mSubject.setColorId(mColor.getId());
            SubjectUtils.replaceSubject(this, mSubject.getId(), mSubject);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SUBJECT, mSubject);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void handleDeleteAction() {
        SubjectUtils.completelyDeleteSubject(this, mSubject);
        setResult(Activity.RESULT_OK);
        finish();
    }

}
