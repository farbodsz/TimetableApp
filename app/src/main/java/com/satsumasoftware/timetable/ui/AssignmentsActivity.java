package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.AssignmentUtils;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.ui.adapter.AssignmentsAdapter;
import com.satsumasoftware.timetable.util.DateUtils;
import com.satsumasoftware.timetable.util.ThemeUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AssignmentsActivity extends BaseActivity {

    protected static final int REQUEST_CODE_ASSIGNMENT_DETAIL = 1;

    protected static final String EXTRA_MODE = "extra_mode";
    protected static final int DISPLAY_TODO = 1;
    protected static final int DISPLAY_ALL_UPCOMING = 2;

    private int mMode;

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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mMode = extras.getInt(EXTRA_MODE);
        } else {
            mMode = DISPLAY_ALL_UPCOMING;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mMode == DISPLAY_TODO) {
            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle(R.string.title_activity_todo);
        }

        ArrayList<Assignment> assignments = AssignmentUtils.getAssignments(this, getApplication());
        if (mMode == DISPLAY_TODO) {
            mAssignments = new ArrayList<>();
            for (Assignment assignment : assignments) {
                if (!assignment.isComplete()) mAssignments.add(assignment);
            }
        } else {
            mAssignments = assignments;
        }

        mHeaders = new ArrayList<>();
        mAssignments = AssignmentUtils.getAssignments(this, getApplication());
        sortList();

        mAdapter = new AssignmentsAdapter(this, mHeaders, mAssignments);
        mAdapter.setOnEntryClickListener(new AssignmentsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(AssignmentsActivity.this, AssignmentDetailActivity.class);
                intent.putExtra(AssignmentDetailActivity.EXTRA_ASSIGNMENT, mAssignments.get(position));

                Bundle bundle = null;
                if (ThemeUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    AssignmentsActivity.this,
                                    view,
                                    getString(R.string.transition_1));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        AssignmentsActivity.this,
                        intent,
                        REQUEST_CODE_ASSIGNMENT_DETAIL,
                        bundle);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                boolean isHeader = mAssignments.get(position) == null;

                int swipeFlags = isHeader ? 0 :
                        ItemTouchHelper.START | ItemTouchHelper.END;

                return makeMovementFlags(0, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                Assignment assignment = mAssignments.get(position);
                assignment.setCompletionProgress(100);
                AssignmentUtils.replaceAssignment(getBaseContext(), assignment.getId(), assignment);

                // Check if assignment is only one in date group
                if (mAssignments.get(position - 1) == null
                        && (mAssignments.size() == position + 1
                        || mAssignments.get(position + 1) == null)) {
                    // Positions either side of the assignment are empty (i.e. headers)
                    int headerPosition = position - 1;

                    // Remove the header from both lists
                    mHeaders.remove(headerPosition);
                    mAssignments.remove(headerPosition);
                    mAdapter.notifyItemRemoved(position);

                    // Update the position of the assignment because we just removed an item
                    position -= 1;
                }

                // Remove the assignment from both lists
                mHeaders.remove(position);
                mAssignments.remove(position);
                mAdapter.notifyItemRemoved(position);

                // No need to refresh the list now, but check if it's empty and needs a placeholder
                refreshPlaceholderStatus();
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;

                ColorDrawable background = new ColorDrawable(
                        ContextCompat.getColor(getBaseContext(), R.color.item_done_background));

                background.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getRight(),
                        itemView.getBottom());

                background.draw(c);

                Bitmap icon =
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_white_24dp);

                float left = dX > 0 ?
                        itemView.getLeft() + ThemeUtils.dpToPixels(getBaseContext(), 16) :
                        itemView.getRight() - ThemeUtils.dpToPixels(getBaseContext(), 16)
                                - icon.getWidth();

                float top = itemView.getTop() +
                        (itemView.getBottom() - itemView.getTop() - icon.getHeight()) / 2;

                c.drawBitmap(icon, left, top, null);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

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
        mAssignments.addAll(AssignmentUtils.getAssignments(this, getApplication()));
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

            if (mMode == DISPLAY_ALL_UPCOMING && assignment.isPastAndDone() && mShowPast) {
                timePeriodId = Integer.parseInt(String.valueOf(dueDate.getYear()) +
                        String.valueOf(dueDate.getMonthValue()));

                if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                    headers.add(dueDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")));
                    assignments.add(null);
                }

                headers.add(null);
                assignments.add(assignment);

                currentTimePeriod = timePeriodId;

            } else if ((mMode == DISPLAY_ALL_UPCOMING && !assignment.isPastAndDone() && !mShowPast)
                    || (mMode == DISPLAY_TODO && !assignment.isComplete())) {
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

        mHeaders.clear();
        mHeaders.addAll(headers);

        mAssignments.clear();
        mAssignments.addAll(assignments);
    }

    private void refreshPlaceholderStatus() {
        if (mAssignments.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mPlaceholderLayout.setVisibility(View.VISIBLE);

            int titleRes = mShowPast ? R.string.placeholder_assignments_past_title :
                    R.string.placeholder_assignments_title;

            int subtitleRes = mShowPast ? R.string.placeholder_assignments_past_subtitle :
                    R.string.placeholder_assignments_subtitle;

            int drawableRes = mShowPast ? R.drawable.ic_assignment_black_24dp :
                    R.drawable.ic_assignment_turned_in_black_24dp;

            mPlaceholderLayout.removeAllViews();
            mPlaceholderLayout.addView(ThemeUtils.makePlaceholderView(this,
                    drawableRes,
                    titleRes,
                    R.color.mdu_blue_400,
                    R.color.mdu_white,
                    R.color.mdu_white,
                    true,
                    subtitleRes));

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
