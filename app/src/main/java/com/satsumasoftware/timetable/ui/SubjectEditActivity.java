package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.DialogInterface;
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
import com.satsumasoftware.timetable.db.SubjectHandler;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.ColorsAdapter;
import com.satsumasoftware.timetable.util.TextUtilsKt;
import com.satsumasoftware.timetable.util.UiUtils;

import java.util.ArrayList;

/**
 * Invoked and displayed to the user for editing the details of a subject, or creating a new one.
 *
 * The user can choose to modify the name, abbreviation, or the {@link Color} associated with the
 * subject.
 *
 * @see Subject
 * @see SubjectsActivity
 */
public class SubjectEditActivity extends AppCompatActivity {

    /**
     * The key for the {@link Subject} passed through an intent extra.
     *
     * It should be null if we're creating a new subject.
     */
    static final String EXTRA_SUBJECT = "extra_subject";

    private Subject mSubject;

    private boolean mIsNew;

    private SubjectHandler mSubjectUtils = new SubjectHandler(this);

    private EditText mEditTextName;
    private EditText mEditTextAbbreviation;

    private Color mColor;
    private AlertDialog mColorDialog;

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
        mIsNew = mSubject == null;

        int titleResId = mIsNew ? R.string.title_activity_subject_new :
                R.string.title_activity_subject_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        toolbar.setNavigationIcon(UiUtils.tintDrawable(this, R.drawable.ic_close_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCloseAction();
            }
        });

        setupLayout(toolbar);
    }

    private void setupLayout(Toolbar toolbar) {
        mEditTextName = (EditText) findViewById(R.id.editText_name);
        if (!mIsNew) {
            mEditTextName.setText(mSubject.getName());
        }

        mEditTextAbbreviation = (EditText) findViewById(R.id.editText_abbreviation);
        if (!mIsNew) {
            mEditTextAbbreviation.setText(mSubject.getAbbreviation());
        }

        setupColorPicker(toolbar);
    }

    private void setupColorPicker(final Toolbar toolbar) {
        mColor = new Color(mIsNew ? 6 : mSubject.getColorId());

        UiUtils.setBarColors(mColor, SubjectEditActivity.this, toolbar);

        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(mColor.getPrimaryColorResId(this));

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
                        UiUtils.setBarColors(mColor, SubjectEditActivity.this, toolbar);
                        mColorDialog.dismiss();
                    }
                });

                RecyclerView recyclerView = new RecyclerView(getBaseContext());
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(SubjectEditActivity.this,
                        getResources().getInteger(R.integer.subject_color_dialog_columns)));
                recyclerView.setAdapter(adapter);

                View titleView =
                        getLayoutInflater().inflate(R.layout.dialog_title_with_padding, null);
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
        String newName = mEditTextName.getText().toString();
        if (newName.length() == 0) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_invalid_name,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        newName = TextUtilsKt.title(newName);

        String newAbbreviation = mEditTextAbbreviation.getText().toString();

        if (mIsNew) {
            Timetable currentTimetable =
                    ((TimetableApplication) getApplication()).getCurrentTimetable();
            assert currentTimetable != null;

            mSubject = new Subject(mSubjectUtils.getHighestItemId() + 1,
                    currentTimetable.getId(),
                    newName,
                    newAbbreviation,
                    mColor.getId());

            mSubjectUtils.addItem(mSubject);

        } else {
            mSubject.setName(newName);
            mSubject.setAbbreviation(newAbbreviation);
            mSubject.setColorId(mColor.getId());
            mSubjectUtils.replaceItem(mSubject.getId(), mSubject);
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SUBJECT, mSubject);
        setResult(Activity.RESULT_OK, intent);
        supportFinishAfterTransition();
    }

    private void handleDeleteAction() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_subject)
                .setMessage(R.string.delete_confirmation_subject)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSubjectUtils.deleteItemWithReferences(mSubject.getId());
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

}
