package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.ThemeUtilsKt;
import com.satsumasoftware.timetable.db.util.ClassUtilsKt;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.ui.adapter.ScheduleAdapter;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScheduleActivity extends BaseActivity {

    protected static final int REQUEST_CODE_CLASS_DETAIL = 1;

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

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            final ArrayList<ClassTime> classTimes = ClassUtilsKt.getClassTimesForDay(this, dayOfWeek);

            if (classTimes.isEmpty()) {
                View placeholder = LayoutInflater.from(this).inflate(R.layout.placeholder_schedule, null);
                mPagerAdapter.addViewWithTitle(placeholder, dayOfWeek.toString());
                continue;
            }

            ScheduleAdapter adapter = new ScheduleAdapter(this, classTimes);
            adapter.setOnEntryClickListener(new ScheduleAdapter.OnEntryClickListener() {
                @Override
                public void onEntryClick(View view, int position) {
                    ClassTime classTime = classTimes.get(position);
                    ClassDetail classDetail =
                            ClassUtilsKt.getClassDetailWithId(getBaseContext(), classTime.getClassDetailId());
                    Class cls =
                            ClassUtilsKt.getClassWithId(getBaseContext(), classDetail.getClassId());

                    Intent intent = new Intent(ScheduleActivity.this, ClassEditActivity.class);
                    intent.putExtra(ClassEditActivity.EXTRA_CLASS, cls);
                    startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
                }
            });

            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setHasFixedSize(true);
            recyclerView.addItemDecoration(new DividerItemDecoration(
                    this, DividerItemDecoration.VERTICAL_LIST));
            recyclerView.setAdapter(adapter);

            mPagerAdapter.addViewWithTitle(recyclerView, dayOfWeek.toString());
        }
    }

    private void goToNow() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        int index = today.getValue() - 1;
        mViewPager.setCurrentItem(index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                setupLayout();
                goToNow();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        ThemeUtilsKt.tintMenuIcons(this, menu, R.id.action_today);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                goToNow();
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
