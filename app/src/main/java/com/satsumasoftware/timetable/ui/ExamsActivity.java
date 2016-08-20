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
import android.widget.TextView;

import com.satsumasoftware.timetable.DateUtilsKt;
import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.AssignmentUtilsKt;
import com.satsumasoftware.timetable.db.util.ExamUtilsKt;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.ui.adapter.AssignmentsAdapter;
import com.satsumasoftware.timetable.ui.adapter.ExamsAdapter;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ExamsActivity extends BaseActivity {

    protected static final int REQUEST_CODE_EXAM_DETAIL = 1;

    private ArrayList<String> mHeaders;
    private ArrayList<Exam> mExams;
    private ExamsAdapter mAdapter;

    private boolean mShowPast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHeaders = new ArrayList<>();
        mExams = ExamUtilsKt.getExams(this);
        sortList();

        mAdapter = new ExamsAdapter(this, mHeaders, mExams);
        mAdapter.setOnEntryClickListener(new ExamsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                //Intent intent = new Intent(ExamsActivity.this, ExamDetailActivity.class);
                //intent.putExtra(ExamDetailActivity.EXTRA_EXAM, mExams.get(position));
                //startActivityForResult(intent, REQUEST_CODE_EXAM_DETAIL);
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
                //Intent intent = new Intent(ExamsActivity.this, ExamDetailActivity.class);
                //startActivityForResult(intent, REQUEST_CODE_EXAM_DETAIL);
            }
        });

    }

    private void refreshList() {
        mExams.clear();
        mExams.addAll(ExamUtilsKt.getExams(this));
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    private void sortList() {
        Collections.sort(mExams, new Comparator<Exam>() {
            @Override
            public int compare(Exam e1, Exam e2) {
                LocalDateTime dateTime1 = e1.makeDateTimeObject();
                LocalDateTime dateTime2 = e2.makeDateTimeObject();
                return dateTime1.compareTo(dateTime2);
            }
        });

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<Exam> exams = new ArrayList<>();

        int currentTimePeriod = -1;

        for (int i = 0; i < mExams.size(); i++) {
            Exam exam = mExams.get(i);

            LocalDate examDate = exam.getDate();
            int timePeriodId;

            if (exam.makeDateTimeObject().isBefore(LocalDateTime.now())) {
                if (mShowPast) {
                    timePeriodId = Integer.parseInt(String.valueOf(examDate.getYear()) +
                            String.valueOf(examDate.getMonthValue()));

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(examDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")));
                        exams.add(null);
                    }

                    headers.add(null);
                    exams.add(exam);

                    currentTimePeriod = timePeriodId;
                }

            } else {

                if (!mShowPast) {
                    timePeriodId = DateUtilsKt.getDatePeriodId(examDate);

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(DateUtilsKt.makeHeaderName(this, timePeriodId));
                        exams.add(null);
                    }

                    headers.add(null);
                    exams.add(exam);

                    currentTimePeriod = timePeriodId;
                }
            }
        }

        mHeaders.clear();
        mHeaders.addAll(headers);

        mExams.clear();
        mExams.addAll(exams);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EXAM_DETAIL) {
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
            case R.id.action_show_past_assignments:
                mShowPast = !mShowPast;
                item.setChecked(mShowPast);

                TextView textView = (TextView) findViewById(R.id.text_infoBar);
                if (mShowPast) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(getString(R.string.showing_past_exams));
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
