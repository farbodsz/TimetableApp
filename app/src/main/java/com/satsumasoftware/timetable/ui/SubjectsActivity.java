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
import com.satsumasoftware.timetable.db.util.SubjectsUtils;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.SubjectsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SubjectsActivity extends BaseActivity {

    protected static final int REQUEST_CODE_SUBJECT_DETAIL = 1;
    protected static final int LIST_POS_INVALID = -1;

    private ArrayList<Subject> mSubjects;
    private SubjectsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSubjects = SubjectsUtils.getSubjects(this);
        sortList();

        mAdapter = new SubjectsAdapter(getBaseContext(), mSubjects);
        mAdapter.setOnEntryClickListener(new SubjectsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(SubjectsActivity.this, SubjectDetailActivity.class);
                intent.putExtra(SubjectDetailActivity.EXTRA_SUBJECT, mSubjects.get(position));
                startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubjectsActivity.this, SubjectDetailActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
            }
        });
    }

    private void refreshList() {
        // change the list itself instead of reassigning so we can do notifyDataSetChanged()
        mSubjects.clear();
        mSubjects.addAll(SubjectsUtils.getSubjects(this));
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    private void sortList() {
        Collections.sort(mSubjects, new Comparator<Subject>() {
            @Override
            public int compare(Subject subject, Subject t1) {
                return subject.getName().compareTo(t1.getName());
            }
        });
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
    protected Toolbar getSelfToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    protected DrawerLayout getSelfDrawerLayout() {
        return (DrawerLayout) findViewById(R.id.drawerLayout);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_SUBJECTS;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
