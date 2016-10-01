package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.ClassUtils;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.ClassesAdapter;
import com.satsumasoftware.timetable.util.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ClassesActivity extends BaseActivity {

    protected static final int REQUEST_CODE_CLASS_DETAIL = 1;

    private ArrayList<Class> mClasses;

    private ClassesAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private FrameLayout mPlaceholderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupLayout();
    }

    private void setupLayout() {
        setupList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassesActivity.this, ClassEditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
            }
        });

        mPlaceholderLayout = (FrameLayout) findViewById(R.id.placeholder);
        refreshPlaceholderStatus();
    }

    private void setupList() {
        mClasses = ClassUtils.getClasses(this);
        sortList();

        mAdapter = new ClassesAdapter(this, mClasses);
        mAdapter.setOnEntryClickListener(new ClassesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(ClassesActivity.this, ClassEditActivity.class);
                intent.putExtra(ClassEditActivity.EXTRA_CLASS, mClasses.get(position));

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

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void sortList() {
        Collections.sort(mClasses, new Comparator<Class>() {
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

    private void refreshList() {
        mClasses.clear();
        mClasses.addAll(ClassUtils.getClasses(this));
        sortList();
        mAdapter.notifyDataSetChanged();
        refreshPlaceholderStatus();
    }

    private void refreshPlaceholderStatus() {
        if (mClasses.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mPlaceholderLayout.setVisibility(View.VISIBLE);

            mPlaceholderLayout.removeAllViews();
            mPlaceholderLayout.addView(UiUtils.makePlaceholderView(this,
                    R.drawable.ic_class_black_24dp,
                    R.string.placeholder_classes,
                    R.color.mdu_blue_400,
                    R.color.mdu_white,
                    R.color.mdu_white,
                    true));

        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mPlaceholderLayout.setVisibility(View.GONE);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_classes, menu);
        UiUtils.tintMenuIcons(this, menu, R.id.action_manage_subjects);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_manage_subjects:
                startActivity(new Intent(this, SubjectsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Toolbar getSelfToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    protected DrawerLayout getSelfDrawerLayout() {
        return (DrawerLayout) findViewById(R.id.drawerLayout);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_CLASSES;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
