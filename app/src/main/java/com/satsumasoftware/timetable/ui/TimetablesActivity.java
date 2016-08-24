package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.ThemeUtils;
import com.satsumasoftware.timetable.db.util.TimetableUtils;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.TimetablesAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TimetablesActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_TIMETABLE_EDIT = 1;

    private ArrayList<Timetable> mTimetables;
    private TimetablesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(ThemeUtils.tintDrawable(this, R.drawable.ic_done_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimetablesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mTimetables = TimetableUtils.getTimetables(this);
        sortList();

        mAdapter = new TimetablesAdapter(getApplication(), mTimetables);
        mAdapter.setOnEntryClickListener(new TimetablesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(TimetablesActivity.this, TimetableEditActivity.class);
                intent.putExtra(TimetableEditActivity.EXTRA_TIMETABLE, mTimetables.get(position));
                startActivityForResult(intent, REQUEST_CODE_TIMETABLE_EDIT);
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
                Intent intent = new Intent(TimetablesActivity.this, TimetableEditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_TIMETABLE_EDIT);
            }
        });
    }

    private void refreshList() {
        mTimetables.clear();
        mTimetables.addAll(TimetableUtils.getTimetables(this));
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    private void sortList() {
        Collections.sort(mTimetables, new Comparator<Timetable>() {
            @Override
            public int compare(Timetable t1, Timetable t2) {
                return t1.getStartDate().compareTo(t2.getStartDate());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TIMETABLE_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList();
            }
        }
    }

}
