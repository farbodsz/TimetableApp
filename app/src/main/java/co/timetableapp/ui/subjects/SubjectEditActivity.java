package co.timetableapp.ui.subjects;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import co.timetableapp.R;
import co.timetableapp.TimetableApplication;
import co.timetableapp.data.handler.SubjectHandler;
import co.timetableapp.model.Color;
import co.timetableapp.model.Subject;
import co.timetableapp.model.Timetable;
import co.timetableapp.ui.base.ItemEditActivity;
import co.timetableapp.util.TextUtilsKt;
import co.timetableapp.util.UiUtils;

/**
 * Allows the user to edit a {@link Subject}.
 *
 * The user can choose to modify the name, abbreviation, or the {@link Color} associated with the
 * subject.
 *
 * @see SubjectsActivity
 * @see ItemEditActivity
 */
public class SubjectEditActivity extends ItemEditActivity<Subject> {

    private SubjectHandler mDataHandler = new SubjectHandler(this);

    private EditText mEditTextName;
    private EditText mEditTextAbbreviation;

    private Color mColor;
    private AlertDialog mColorDialog;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_subject_edit;
    }

    @Override
    protected int getTitleRes(boolean isNewItem) {
        return isNewItem ? R.string.title_activity_subject_new :
                R.string.title_activity_subject_edit;
    }

    @Override
    protected void setupLayout() {
        mEditTextName = (EditText) findViewById(R.id.editText_name);
        if (!mIsNew) {
            mEditTextName.setText(mItem.getName());
        }

        mEditTextAbbreviation = (EditText) findViewById(R.id.editText_abbreviation);
        if (!mIsNew) {
            mEditTextAbbreviation.setText(mItem.getAbbreviation());
        }

        setupColorPicker();
    }

    private void setupColorPicker() {
        mColor = new Color(mIsNew ? 6 : mItem.getColorId());

        UiUtils.setBarColors(mColor, SubjectEditActivity.this, mToolbar);

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
                        UiUtils.setBarColors(mColor, SubjectEditActivity.this, mToolbar);
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
    protected void handleDoneAction() {
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

            mItem = new Subject(mDataHandler.getHighestItemId() + 1,
                    currentTimetable.getId(),
                    newName,
                    newAbbreviation,
                    mColor.getId());

            mDataHandler.addItem(mItem);

        } else {
            mItem.setName(newName);
            mItem.setAbbreviation(newAbbreviation);
            mItem.setColorId(mColor.getId());
            mDataHandler.replaceItem(mItem.getId(), mItem);
        }

        Intent intent = new Intent();
        intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItem);
        setResult(Activity.RESULT_OK, intent);
        supportFinishAfterTransition();
    }

    @Override
    protected void handleDeleteAction() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_subject)
                .setMessage(R.string.delete_confirmation_subject)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDataHandler.deleteItemWithReferences(mItem.getId());
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

}
