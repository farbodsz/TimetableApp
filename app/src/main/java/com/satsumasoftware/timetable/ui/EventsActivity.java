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
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.EventUtils;
import com.satsumasoftware.timetable.framework.Event;
import com.satsumasoftware.timetable.ui.adapter.EventsAdapter;
import com.satsumasoftware.timetable.util.DateUtils;
import com.satsumasoftware.timetable.util.UiUtils;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * For displaying a list of events to the user.
 *
 * If there are no events to display, a placeholder background will be shown instead.
 *
 * Clicking on an event to view or edit, or choosing to create a new event will direct the user to
 * {@link EventEditActivity}.
 *
 * @see Event
 */
public class EventsActivity extends BaseActivity {

    private static final int REQUEST_CODE_EVENT_EDIT = 1;

    private ArrayList<String> mHeaders;
    private ArrayList<Event> mEvents;

    private EventsAdapter mAdapter;

    private RecyclerView mRecyclerView;
    private FrameLayout mPlaceholderLayout;

    private boolean mShowPast;

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
                Intent intent = new Intent(EventsActivity.this, EventEditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_EVENT_EDIT);
            }
        });

        mPlaceholderLayout = (FrameLayout) findViewById(R.id.placeholder);
        refreshPlaceholderStatus();
    }

    private void setupList() {
        mHeaders = new ArrayList<>();
        mEvents = EventUtils.getEvents(this, getApplication());
        sortList();

        mAdapter = new EventsAdapter(this, mHeaders, mEvents);
        mAdapter.setOnEntryClickListener(new EventsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(EventsActivity.this, EventEditActivity.class);
                intent.putExtra(EventEditActivity.EXTRA_EVENT, mEvents.get(position));

                Bundle bundle = null;
                if (UiUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    EventsActivity.this,
                                    view,
                                    getString(R.string.transition_1));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        EventsActivity.this, intent, REQUEST_CODE_EVENT_EDIT, bundle);
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
        Collections.sort(mEvents, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                LocalDateTime dateTime1 = e1.getStartTime();
                LocalDateTime dateTime2 = e2.getStartTime();
                if (mShowPast) {
                    return dateTime2.compareTo(dateTime1);
                } else {
                    return dateTime1.compareTo(dateTime2);
                }
            }
        });

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<Event> events = new ArrayList<>();

        int currentTimePeriod = -1;

        for (int i = 0; i < mEvents.size(); i++) {
            Event event = mEvents.get(i);

            LocalDateTime eventDate = event.getStartTime();
            int timePeriodId;

            if (eventDate.isBefore(LocalDateTime.now())) {
                if (mShowPast) {
                    timePeriodId = Integer.parseInt(String.valueOf(eventDate.getYear()) +
                            String.valueOf(eventDate.getMonthValue()));

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(eventDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")));
                        events.add(null);
                    }

                    headers.add(null);
                    events.add(event);

                    currentTimePeriod = timePeriodId;
                }

            } else {

                if (!mShowPast) {
                    timePeriodId = DateUtils.getDatePeriodId(eventDate.toLocalDate());

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(DateUtils.makeHeaderName(this, timePeriodId));
                        events.add(null);
                    }

                    headers.add(null);
                    events.add(event);

                    currentTimePeriod = timePeriodId;
                }
            }
        }

        mHeaders.clear();
        mHeaders.addAll(headers);

        mEvents.clear();
        mEvents.addAll(events);
    }

    private void refreshList() {
        mEvents.clear();
        mEvents.addAll(EventUtils.getEvents(this, getApplication()));
        sortList();
        mAdapter.notifyDataSetChanged();
        refreshPlaceholderStatus();
    }

    private void refreshPlaceholderStatus() {
        if (mEvents.isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mPlaceholderLayout.setVisibility(View.VISIBLE);

            int stringRes = mShowPast ? R.string.placeholder_events_past :
                    R.string.placeholder_events;

            mPlaceholderLayout.removeAllViews();
            mPlaceholderLayout.addView(UiUtils.makePlaceholderView(this,
                    R.drawable.ic_event_black_24dp,
                    stringRes,
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

        if (requestCode == REQUEST_CODE_EVENT_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_assignments, menu);
        menu.findItem(R.id.action_show_past).setTitle(getString(R.string.action_show_past_events));
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
                    textView.setText(getString(R.string.showing_past_events));
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
        return NAVDRAWER_ITEM_EVENTS;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
