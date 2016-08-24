package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.ClassUtils;
import com.satsumasoftware.timetable.db.util.SubjectUtils;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.ClassesAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ClassesActivity extends BaseActivity {

    protected static final int REQUEST_CODE_CLASS_DETAIL = 1;

    private ArrayList<Class> mClasses;
    private ClassesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mClasses = ClassUtils.getClasses(this);
        sortList();

        mAdapter = new ClassesAdapter(this, mClasses);
        mAdapter.setOnEntryClickListener(new ClassesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(ClassesActivity.this, ClassEditActivity.class);
                intent.putExtra(ClassEditActivity.EXTRA_CLASS, mClasses.get(position));
                startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassesActivity.this, ClassEditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
            }
        });
    }

    private void refreshList() {
        mClasses.clear();
        mClasses.addAll(ClassUtils.getClasses(this));
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    private void sortList() {
        Collections.sort(mClasses, new Comparator<Class>() {
            @Override
            public int compare(Class c1, Class c2) {
                Subject s1 = SubjectUtils.getSubjectWithId(getBaseContext(), c1.getSubjectId());
                Subject s2 = SubjectUtils.getSubjectWithId(getBaseContext(), c2.getSubjectId());
                assert s1 != null;
                assert s2 != null;
                return s1.getName().compareTo(s2.getName());
            }
        });
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
