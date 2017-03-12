package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.handler.ClassHandler;
import com.satsumasoftware.timetable.db.handler.TimetableItemHandler;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.ClassesAdapter;
import com.satsumasoftware.timetable.util.UiUtils;

import java.util.Collections;
import java.util.Comparator;

/**
 * An activity for displaying a list of classes to the user.
 *
 * If there are no classes to display, a placeholder background will be shown instead.
 *
 * Clicking on a class will allow the user to view its details in {@link ClassDetailActivity}.
 * The user can also choose to create a new class in which case {@link ClassDetailActivity}
 * will also be invoked but with no intent extra data.
 *
 * @see Class
 * @see ClassDetailActivity
 * @see ClassEditActivity
 */
public class ClassesActivity extends ItemListActivity<Class> {

    private static final int REQUEST_CODE_CLASS_DETAIL = 1;

    @Override
    TimetableItemHandler<Class> instantiateDataHandler() {
        return new ClassHandler(this);
    }

    @Override
    void onFabButtonClick() {
        Intent intent = new Intent(ClassesActivity.this, ClassEditActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
    }

    @Override
    RecyclerView.Adapter setupAdapter() {
        ClassesAdapter adapter = new ClassesAdapter(this, mItems);
        adapter.setOnEntryClickListener(new ClassesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(ClassesActivity.this, ClassDetailActivity.class);
                intent.putExtra(ClassDetailActivity.EXTRA_CLASS, mItems.get(position));

                Bundle bundle = null;
                if (UiUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    ClassesActivity.this,
                                    view,
                                    getString(R.string.transition_1));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        ClassesActivity.this, intent, REQUEST_CODE_CLASS_DETAIL, bundle);
            }
        });

        return adapter;
    }

    @Override
    void sortList() {
        Collections.sort(mItems, new Comparator<Class>() {
            @Override
            public int compare(Class c1, Class c2) {
                Subject s1 = Subject.create(getBaseContext(), c1.getSubjectId());
                Subject s2 = Subject.create(getBaseContext(), c2.getSubjectId());
                assert s1 != null;
                assert s2 != null;
                return s1.getName().compareTo(s2.getName());
            }
        });
    }

    @Override
    View getPlaceholderView() {
        return UiUtils.makePlaceholderView(this,
                R.drawable.ic_class_black_24dp,
                R.string.placeholder_classes,
                R.color.mdu_blue_400,
                R.color.mdu_white,
                R.color.mdu_white,
                true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList();
            }
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_CLASSES;
    }

}
