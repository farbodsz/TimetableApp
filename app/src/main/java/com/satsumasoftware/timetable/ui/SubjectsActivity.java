package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.SubjectUtils;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.SubjectsAdapter;
import com.satsumasoftware.timetable.util.UiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SubjectsActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_SUBJECT_DETAIL = 1;

    private ArrayList<Subject> mSubjects;

    private SubjectsAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private FrameLayout mPlaceholderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(UiUtils.tintDrawable(this, R.drawable.ic_close_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupLayout();
    }

    private void setupLayout() {
        setupList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubjectsActivity.this, SubjectEditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
            }
        });

        mPlaceholderLayout = (FrameLayout) findViewById(R.id.placeholder);
        refreshPlaceholderStatus();
    }

    private void setupList() {
        mSubjects = SubjectUtils.getSubjects(this);
        sortList();

        mAdapter = new SubjectsAdapter(this, mSubjects);
        mAdapter.setOnEntryClickListener(new SubjectsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(SubjectsActivity.this, SubjectEditActivity.class);
                intent.putExtra(SubjectEditActivity.EXTRA_SUBJECT, mSubjects.get(position));

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

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void sortList() {
        Collections.sort(mSubjects, new Comparator<Subject>() {
            @Override
            public int compare(Subject subject, Subject t1) {
                return subject.getName().compareTo(t1.getName());
            }
        });
    }

    private void refreshList() {
        // change the list itself instead of reassigning so we can do notifyDataSetChanged()
        mSubjects.clear();
        mSubjects.addAll(SubjectUtils.getSubjects(this));
        sortList();
        mAdapter.notifyDataSetChanged();
        refreshPlaceholderStatus();
    }

    private void refreshPlaceholderStatus() {
        if (mSubjects.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mPlaceholderLayout.setVisibility(View.VISIBLE);

            mPlaceholderLayout.removeAllViews();
            mPlaceholderLayout.addView(UiUtils.makePlaceholderView(this,
                    R.drawable.ic_list_black_24dp,
                    R.string.placeholder_subjects,
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

        if (requestCode == REQUEST_CODE_SUBJECT_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList();
            }
        }
    }

}
