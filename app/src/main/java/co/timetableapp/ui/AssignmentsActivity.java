package co.timetableapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import co.timetableapp.R;
import co.timetableapp.data.handler.AssignmentHandler;
import co.timetableapp.data.handler.TimetableItemHandler;
import co.timetableapp.framework.Assignment;
import co.timetableapp.ui.adapter.AssignmentsAdapter;
import co.timetableapp.util.DateUtils;
import co.timetableapp.util.UiUtils;

/**
 * An activity for displaying a list of assignments to the user.
 *
 * Assignments can be displayed in two formats: {@link #DISPLAY_TODO} and
 * {@link #DISPLAY_ALL_UPCOMING}. In the former, only incomplete assignments will be displayed; in
 * the latter, only assignments that are due in the future (regardless of completion) and overdue
 * assignments will be shown.
 *
 * If there are no assignments to display, a placeholder background will be shown instead.
 *
 * Clicking on an assignment will allow the user to view its details in
 * {@link AssignmentDetailActivity}. The user can also choose to create a new assignment in which
 * case {@link AssignmentDetailActivity} will also be invoked but with no intent extra data.
 *
 * @see Assignment
 * @see AssignmentDetailActivity
 * @see AssignmentEditActivity
 */
public class AssignmentsActivity extends ItemListActivity<Assignment> {

    private static final int REQUEST_CODE_ASSIGNMENT_DETAIL = 1;

    /**
     * The intent extra key for the display mode of the assignments.
     *
     * This should be either {@link #DISPLAY_TODO} or {@link #DISPLAY_ALL_UPCOMING}. If the data
     * passed with this key is null, {@link #DISPLAY_ALL_UPCOMING} will be used by default.
     */
    public static final String EXTRA_MODE = "extra_mode";

    /**
     * Suggests that only incomplete assignments will be shown in the list.
     *
     * It is specified by passing it through an intent extra with the {@link #EXTRA_MODE} key.
     *
     * @see #DISPLAY_ALL_UPCOMING
     */
    public static final int DISPLAY_TODO = 1;

    /**
     * Suggests that only assignments due in the future and overdue assignments will be shown in the
     * list.
     *
     * It is specified by passing it through an intent extra with the {@link #EXTRA_MODE} key.
     *
     * @see #DISPLAY_TODO
     */
    public static final int DISPLAY_ALL_UPCOMING = 2;

    private int mMode;

    private ArrayList<String> mHeaders;

    private boolean mShowPast;

    @Override
    TimetableItemHandler<Assignment> instantiateDataHandler() {
        return new AssignmentHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        determineDisplayMode();
        super.onCreate(savedInstanceState);
    }

    @Override
    void setupToolbar() {
        super.setupToolbar();

        if (mMode == DISPLAY_TODO) {
            assert getSupportActionBar() != null;
            getSupportActionBar().setTitle(R.string.title_activity_todo);
        }
    }

    private void determineDisplayMode() {
        if (mMode != 0) {
            return;
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mMode = extras.getInt(EXTRA_MODE);
        } else {
            mMode = DISPLAY_ALL_UPCOMING;
        }
    }

    @Override
    void onFabButtonClick() {
        Intent intent = new Intent(AssignmentsActivity.this, AssignmentDetailActivity.class);
        startActivityForResult(intent, REQUEST_CODE_ASSIGNMENT_DETAIL);
    }

    @Override
    void setupList() {
        mHeaders = new ArrayList<>();
        super.setupList();
        makeItemTouchHelper().attachToRecyclerView(mRecyclerView);
    }

    @Override
    RecyclerView.Adapter setupAdapter() {
        AssignmentsAdapter adapter = new AssignmentsAdapter(this, mHeaders, mItems);
        adapter.setOnEntryClickListener(new AssignmentsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(AssignmentsActivity.this, AssignmentDetailActivity.class);
                intent.putExtra(AssignmentDetailActivity.EXTRA_ITEM, mItems.get(position));

                Bundle bundle = null;
                if (UiUtils.isApi21()) {
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

        return adapter;
    }

    private ItemTouchHelper makeItemTouchHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.Callback() {
            private String mRemovedHeader;
            private Assignment mRemovedAssignment;
            private int mRemovedAssignmentPos;
            private int mRemovedCompletionProgress;

            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                boolean isHeader = mItems.get(position) == null;

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

                Assignment assignment = mItems.get(position);

                // Store the assignment we're about to remove for the undo action
                mRemovedHeader = null;
                mRemovedAssignmentPos = position;
                mRemovedAssignment = assignment;
                mRemovedCompletionProgress = assignment.getCompletionProgress();

                assignment.setCompletionProgress(100);
                mDataHandler.replaceItem(assignment.getId(), assignment);

                // Do not completely remove the item if we're not in DISPLAY_TODO mode
                if (mMode != DISPLAY_TODO) {
                    // We should remove and add back the item so the 'done' background goes away
                    // and the item gets updated
                    mItems.remove(position);
                    mAdapter.notifyItemRemoved(position);
                    mItems.add(position, mRemovedAssignment);
                    mAdapter.notifyItemInserted(position);

                    final int finalPos = position;

                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.message_assignment_completed,
                            Snackbar.LENGTH_SHORT)
                            .setAction(R.string.action_undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Assignment assignment = mRemovedAssignment;
                                    assignment.setCompletionProgress(mRemovedCompletionProgress);

                                    mDataHandler.replaceItem(assignment.getId(), assignment);

                                    mItems.set(finalPos, assignment);
                                    mAdapter.notifyItemChanged(finalPos);
                                }
                            })
                            .show();
                    return;
                }

                // Check if assignment is only one in date group
                if (mItems.get(position - 1) == null
                        && (mItems.size() == position + 1
                        || mItems.get(position + 1) == null)) {
                    // Positions either side of the assignment are empty (i.e. headers)
                    int headerPosition = position - 1;

                    // Store the header we're about to remove for the undo action
                    mRemovedHeader = mHeaders.get(headerPosition);

                    // Remove the header from both lists
                    mHeaders.remove(headerPosition);
                    mItems.remove(headerPosition);
                    mAdapter.notifyItemRemoved(position);

                    // Update the position of the assignment because we just removed an item
                    position -= 1;
                }

                // Remove the assignment from both lists
                mHeaders.remove(position);
                mItems.remove(position);
                mAdapter.notifyItemRemoved(position);

                // Show a Snackbar with the undo action
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.message_assignment_completed,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.action_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mRemovedHeader != null) {
                                    mHeaders.add(mRemovedAssignmentPos - 1, mRemovedHeader);
                                    mItems.add(mRemovedAssignmentPos - 1, null);
                                    mAdapter.notifyItemInserted(mRemovedAssignmentPos - 1);
                                }

                                Assignment assignment = mRemovedAssignment;
                                assignment.setCompletionProgress(mRemovedCompletionProgress);

                                mHeaders.add(mRemovedAssignmentPos, null);
                                mItems.add(mRemovedAssignmentPos, assignment);
                                mAdapter.notifyItemInserted(mRemovedAssignmentPos);

                                mDataHandler.replaceItem(assignment.getId(), assignment);

                                refreshPlaceholderStatus();
                            }
                        })
                        .show();

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
                        itemView.getLeft() + UiUtils.dpToPixels(getBaseContext(), 16) :
                        itemView.getRight() - UiUtils.dpToPixels(getBaseContext(), 16)
                                - icon.getWidth();

                float top = itemView.getTop() +
                        (itemView.getBottom() - itemView.getTop() - icon.getHeight()) / 2;

                c.drawBitmap(icon, left, top, null);

                super.onChildDraw(c, recyclerView, viewHolder,
                        dX, dY, actionState, isCurrentlyActive);
            }
        });
    }

    @Override
    void sortList() {
        Collections.sort(mItems, new Comparator<Assignment>() {
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

        for (int i = 0; i < mItems.size(); i++) {
            Assignment assignment = mItems.get(i);

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

        mItems.clear();
        mItems.addAll(assignments);
    }

    @Override
    View getPlaceholderView() {
        int titleRes = mShowPast ? R.string.placeholder_assignments_past_title :
                R.string.placeholder_assignments_title;

        int subtitleRes;
        if (mMode == DISPLAY_TODO) {
            subtitleRes = R.string.placeholder_assignments_todo_subtitle;
        } else {
            subtitleRes = mShowPast ? R.string.placeholder_assignments_past_subtitle :
                    R.string.placeholder_assignments_upcoming_subtitle;
        }

        int drawableRes = mShowPast ? R.drawable.ic_assignment_black_24dp :
                R.drawable.ic_assignment_turned_in_black_24dp;

        return UiUtils.makePlaceholderView(this,
                drawableRes,
                titleRes,
                R.color.mdu_blue_400,
                R.color.mdu_white,
                R.color.mdu_white,
                true,
                subtitleRes);
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
        // Do not show the menu in DISPLAY_TODO mode
        if (mMode == DISPLAY_TODO) {
            return false;
        }

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
    protected int getSelfNavDrawerItem() {
        determineDisplayMode();
        return mMode == DISPLAY_ALL_UPCOMING ? NAVDRAWER_ITEM_ASSIGNMENTS : NAVDRAWER_ITEM_TODO;
    }

}
