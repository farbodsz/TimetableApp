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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.satsumasoftware.timetable.DateUtils;
import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.ThemeUtils;
import com.satsumasoftware.timetable.db.util.AssignmentUtils;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.ui.adapter.AssignmentsAdapter;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AssignmentsActivity extends BaseActivity {

    protected static final int REQUEST_CODE_ASSIGNMENT_DETAIL = 1;

    private ArrayList<String> mHeaders;
    private ArrayList<Assignment> mAssignments;
    private AssignmentsAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private FrameLayout mPlaceholderLayout;

    private boolean mShowPast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHeaders = new ArrayList<>();
        mAssignments = AssignmentUtils.getAssignments(this);
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

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AssignmentsActivity.this, AssignmentDetailActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_DETAIL);
            }
        });

        mPlaceholderLayout = (FrameLayout) findViewById(R.id.placeholder);
        refreshPlaceholderStatus();
    }

    private void refreshList() {
        mAssignments.clear();
        mAssignments.addAll(AssignmentUtils.getAssignments(this));
        sortList();
        mAdapter.notifyDataSetChanged();
        refreshPlaceholderStatus();
    }

    private void sortList() {
        Collections.sort(mAssignments, new Comparator<Assignment>() {
            @Override
            public int compare(Assignment a1, Assignment a2) {
                LocalDate dueDate1 = a1.getDueDate();
                LocalDate dueDate2 = a2.getDueDate();
                if (mShowPast) {
                    return dueDate2.compareTo(dueDate1);
                } else {
                    return dueDate1.compareTo(dueDate2);
                }
            }
        });

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<Assignment> assignments = new ArrayList<>();

        int currentTimePeriod = -1;

        for (int i = 0; i < mAssignments.size(); i++) {
            Assignment assignment = mAssignments.get(i);

            LocalDate dueDate = assignment.getDueDate();
            int timePeriodId;

            if (dueDate.isBefore(LocalDate.now()) && assignment.getCompletionProgress() == 100) {
                if (mShowPast) {
                    timePeriodId = Integer.parseInt(String.valueOf(dueDate.getYear()) +
                            String.valueOf(dueDate.getMonthValue()));

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(dueDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")));
                        assignments.add(null);
                    }

                    headers.add(null);
                    assignments.add(assignment);

                    currentTimePeriod = timePeriodId;
                }

            } else {

                if (!mShowPast) {
                    timePeriodId = DateUtils.getDatePeriodId(dueDate);

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(DateUtils.makeHeaderName(this, timePeriodId));
                        assignments.add(null);
                    }

                    headers.add(null);
                    assignments.add(assignment);

                    currentTimePeriod = timePeriodId;
                }
            }
        }

        mHeaders.clear();
        mHeaders.addAll(headers);

        mAssignments.clear();
        mAssignments.addAll(assignments);
    }

    private void refreshPlaceholderStatus() {
        if (mAssignments.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mPlaceholderLayout.setVisibility(View.VISIBLE);

            int stringRes = mShowPast ? R.string.placeholder_assignments_past :
                    R.string.placeholder_assignments;
            int drawableRes = mShowPast ? R.drawable.ic_assignment_black_24dp :
                    R.drawable.ic_assignment_turned_in_black_24dp;

            mPlaceholderLayout.removeAllViews();
            mPlaceholderLayout.addView(ThemeUtils.makePlaceholderView(this,
                    drawableRes, stringRes));

        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mPlaceholderLayout.setVisibility(View.GONE);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_assignments, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_past:
                mShowPast = !mShowPast;
                item.setChecked(mShowPast);

                TextView textView = (TextView) findViewById(R.id.text_infoBar);
                if (mShowPast) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.showing_past_assignments));
                } else {
                    textView.setVisibility(View.GONE);
                }
                refreshList();
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
        return NAVDRAWER_ITEM_ASSIGNMENTS;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
