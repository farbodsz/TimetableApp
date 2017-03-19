package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.data.handler.SubjectHandler;
import com.satsumasoftware.timetable.data.handler.TimetableItemHandler;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.SubjectsAdapter;
import com.satsumasoftware.timetable.util.UiUtils;

import java.util.Collections;
import java.util.Comparator;

/**
 * An activity for displaying a list of subjects to the user.
 *
 * If there are no subjects to display, a placeholder background will be shown instead.
 *
 * Clicking on a subject to view or edit, or choosing to create a new subject will direct the user
 * to {@link SubjectEditActivity}.
 *
 * @see Subject
 * @see SubjectEditActivity
 */
public class SubjectsActivity extends ItemListActivity<Subject> {

    private static final int REQUEST_CODE_SUBJECT_DETAIL = 1;

    @Override
    TimetableItemHandler<Subject> instantiateDataHandler() {
        return new SubjectHandler(this);
    }

    @Override
    void onFabButtonClick() {
        Intent intent = new Intent(SubjectsActivity.this, SubjectEditActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
    }

    @Override
    RecyclerView.Adapter setupAdapter() {
        SubjectsAdapter adapter = new SubjectsAdapter(this, mItems);
        adapter.setOnEntryClickListener(new SubjectsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(SubjectsActivity.this, SubjectEditActivity.class);
                intent.putExtra(SubjectEditActivity.EXTRA_SUBJECT, mItems.get(position));

                Bundle bundle = null;
                if (UiUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    SubjectsActivity.this,
                                    view,
                                    getString(R.string.transition_1));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        SubjectsActivity.this, intent, REQUEST_CODE_SUBJECT_DETAIL, bundle);
            }
        });

        return adapter;
    }

    @Override
    void sortList() {
        Collections.sort(mItems, new Comparator<Subject>() {
            @Override
            public int compare(Subject subject, Subject t1) {
                return subject.getName().compareTo(t1.getName());
            }
        });
    }

    @Override
    View getPlaceholderView() {
        return UiUtils.makePlaceholderView(this,
                R.drawable.ic_list_black_24dp,
                R.string.placeholder_subjects,
                R.color.mdu_blue_400,
                R.color.mdu_white,
                R.color.mdu_white,
                true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SUBJECT_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList();
            }
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_SUBJECTS;
    }

}
