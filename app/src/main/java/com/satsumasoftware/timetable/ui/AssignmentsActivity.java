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

import com.satsumasoftware.timetable.DateUtilsKt;
import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.AssignmentUtilsKt;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.ui.adapter.AssignmentsAdapter;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AssignmentsActivity extends BaseActivity {

    protected static final int REQUEST_CODE_ASSIGNMENT_DETAIL = 1;

    private ArrayList<String> mHeaders;
    private ArrayList<Assignment> mAssignments;
    private AssignmentsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHeaders = new ArrayList<>();
        mAssignments = AssignmentUtilsKt.getAssignments(this);
        sortList();

        mAdapter = new AssignmentsAdapter(this, mHeaders, mAssignments);
        mAdapter.setOnEntryClickListener(new AssignmentsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(AssignmentsActivity.this, AssignmentDetailActivity.class);
                intent.putExtra(AssignmentDetailActivity.EXTRA_ASSIGNMENT, mAssignments.get(position));
                startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_DETAIL);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AssignmentsActivity.this, AssignmentDetailActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_DETAIL);
            }
        });

    }

    private void refreshList() {
        mAssignments.clear();
        mAssignments.addAll(AssignmentUtilsKt.getAssignments(this));
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    private void sortList() {
        Collections.sort(mAssignments, new Comparator<Assignment>() {
            @Override
            public int compare(Assignment a1, Assignment a2) {
                LocalDate dueDate1 = a1.getDueDate();
                LocalDate dueDate2 = a2.getDueDate();
                return dueDate1.compareTo(dueDate2);
            }
        });

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<Assignment> assignments = new ArrayList<>();

        int currentTimePeriod = -1;

        for (int i = 0; i < mAssignments.size(); i++) {
            Assignment assignment = mAssignments.get(i);
            LocalDate dueDate = assignment.getDueDate();
            int timePeriodId = DateUtilsKt.getDatePeriodId(dueDate);

            if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                headers.add(DateUtilsKt.makeHeaderName(this, timePeriodId));
                assignments.add(null);
            }

            headers.add(null);
            assignments.add(assignment);

            currentTimePeriod = timePeriodId;
        }

        mHeaders.clear();
        mHeaders.addAll(headers);

        mAssignments.clear();
        mAssignments.addAll(assignments);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ASSIGNMENT_DETAIL) {
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
        return NAVDRAWER_ITEM_ASSIGNMENTS;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
