package co.timetableapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import co.timetableapp.R;
import co.timetableapp.TimetableApplication;
import co.timetableapp.db.handler.ClassTimeHandler;
import co.timetableapp.framework.Class;
import co.timetableapp.framework.ClassDetail;
import co.timetableapp.framework.ClassTime;
import co.timetableapp.framework.Timetable;
import co.timetableapp.ui.adapter.ScheduleAdapter;
import co.timetableapp.util.DateUtils;
import co.timetableapp.util.UiUtils;

/**
 * An activity for displaying a weekly schedule.
 *
 * Each day is a tab in the layout, and each tab will display a list of classes for that day.
 */
public class ScheduleActivity extends NavigationDrawerActivity {

    private static final String LOG_TAG = "ScheduleActivity";

    private static final int REQUEST_CODE_CLASS_DETAIL = 1;

    private DynamicPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        mPagerAdapter = new DynamicPagerAdapter();

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(mPagerAdapter);

        setupLayout();

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white));
        tabLayout.setupWithViewPager(mViewPager);

        goToNow();
    }

    private void setupLayout() {
        mPagerAdapter.removeAllViews(mViewPager);

        Timetable timetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert timetable != null;

        if (!timetable.isValidToday()) {
            View placeholder = UiUtils.makePlaceholderView(this,
                    R.drawable.ic_today_black_24dp, R.string.home_card_classes_placeholder);
            mPagerAdapter.addViewWithTitle(
                    placeholder, getString(R.string.title_activity_schedule));
            return;
        }

        LocalDate today = LocalDate.now();
        int indexOfToday = getIndexOfTodayTab();

        int daysCount = 0;

        for (int weekNumber = 1; weekNumber <= timetable.getWeekRotations(); weekNumber++) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {

                LocalDate thisDay;
                if (indexOfToday > daysCount) {
                    // This day is before today
                    thisDay = today.minusDays(indexOfToday - daysCount);

                } else if (indexOfToday == daysCount) {
                    // This day is today
                    thisDay = today;

                } else {
                    // This day is after today
                    thisDay = today.plusDays(daysCount - indexOfToday);
                }
                Log.i(LOG_TAG, "Finding lessons for " + thisDay.toString());

                StringBuilder titleBuilder = new StringBuilder();
                titleBuilder.append(dayOfWeek.toString());
                if (!timetable.hasFixedScheduling()) {
                    titleBuilder.append(" ")
                            .append(ClassTime.getWeekText(this, weekNumber, false));
                }
                String tabTitle = titleBuilder.toString();

                final ArrayList<ClassTime> classTimes =
                        ClassTimeHandler.getClassTimesForDay(this, dayOfWeek, weekNumber, thisDay);

                if (classTimes.isEmpty()) {
                    View placeholder = UiUtils.makePlaceholderView(this,
                            R.drawable.ic_today_black_24dp, R.string.home_card_classes_placeholder);
                    mPagerAdapter.addViewWithTitle(placeholder, tabTitle);
                    daysCount++;
                    continue;
                }

                Collections.sort(classTimes, new Comparator<ClassTime>() {
                    @Override
                    public int compare(ClassTime t1, ClassTime t2) {
                        int startTimeComparison = t1.getStartTime().compareTo(t2.getStartTime());
                        if (startTimeComparison == 0) {
                            return t1.getEndTime().compareTo(t2.getEndTime());
                        }
                        return startTimeComparison;
                    }
                });

                ScheduleAdapter adapter = new ScheduleAdapter(this, classTimes);
                adapter.setOnEntryClickListener(new ScheduleAdapter.OnEntryClickListener() {
                    @Override
                    public void onEntryClick(View view, int position) {
                        ClassTime classTime = classTimes.get(position);
                        ClassDetail classDetail = ClassDetail.create(
                                getBaseContext(), classTime.getClassDetailId());
                        Class cls = Class.create(getBaseContext(), classDetail.getClassId());

                        Intent intent = new Intent(ScheduleActivity.this, ClassEditActivity.class);
                        intent.putExtra(ClassEditActivity.EXTRA_CLASS, cls);
                        intent.putExtra(ClassEditActivity.EXTRA_CLASS_DETAIL_ID,
                                classDetail.getId());

                        Bundle bundle = null;
                        if (UiUtils.isApi21()) {
                            ActivityOptionsCompat options =
                                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                                            ScheduleActivity.this,
                                            view,
                                            getString(R.string.transition_1));
                            bundle = options.toBundle();
                        }

                        ActivityCompat.startActivityForResult(
                                ScheduleActivity.this, intent, REQUEST_CODE_CLASS_DETAIL, bundle);
                    }
                });

                RecyclerView recyclerView = new RecyclerView(this);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setHasFixedSize(true);
                recyclerView.addItemDecoration(new DividerItemDecoration(
                        this, DividerItemDecoration.VERTICAL_LIST));
                recyclerView.setAdapter(adapter);

                mPagerAdapter.addViewWithTitle(recyclerView, tabTitle);

                daysCount++;
            }
        }
    }

    private int getIndexOfTodayTab() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        int nthWeek = DateUtils.findWeekNumber(getApplication());
        return today.getValue() + ((nthWeek - 1) * 7) - 1;
    }

    private void goToNow() {
        mViewPager.setCurrentItem(getIndexOfTodayTab());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == RESULT_OK) {
                setupLayout();
                goToNow();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        UiUtils.tintMenuIcons(this, menu, R.id.action_today, R.id.action_view_week);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                goToNow();
                break;
            case R.id.action_view_week:
                Snackbar.make(findViewById(R.id.drawerLayout), "Week view coming soon",
                        Snackbar.LENGTH_SHORT).show();
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
        return NAVDRAWER_ITEM_SCHEDULE;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
