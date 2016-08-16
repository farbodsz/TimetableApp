package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.ClassesSchema;
import com.satsumasoftware.timetable.db.TimetableDbHelper;
import com.satsumasoftware.timetable.db.util.ClassUtilsKt;
import com.satsumasoftware.timetable.db.util.SubjectUtilsKt;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Color;
import com.satsumasoftware.timetable.framework.Subject;

import org.threeten.bp.DayOfWeek;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ScheduleActivity extends BaseActivity {

    protected static final int REQUEST_CODE_CLASS_DETAIL = 1;

    private WeekView mWeekView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mWeekView = (WeekView) findViewById(R.id.weekView);

        mWeekView.setOnEventClickListener(new WeekView.EventClickListener() {
            @Override
            public void onEventClick(WeekViewEvent event, RectF eventRect) {
                TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(ScheduleActivity.this);
                Cursor cursor = dbHelper.getReadableDatabase().query(
                        ClassesSchema.TABLE_NAME,
                        null,
                        ClassesSchema._ID + "=?",
                        new String[] {String.valueOf(event.getId())},
                        null, null, null);
                cursor.moveToFirst();
                Class cls = new Class(getBaseContext(), cursor);
                cursor.close();
                Intent intent = new Intent(ScheduleActivity.this, ClassDetailActivity.class);
                intent.putExtra(ClassDetailActivity.EXTRA_CLASS, cls);
                startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
            }
        });

        populateLayout();

        goToNow();
    }

    private void populateLayout() {
        mWeekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                return getEvents(newYear, newMonth);
            }
        });
    }

    private List<WeekViewEvent> getEvents(int year, int month) {
        ArrayList<WeekViewEvent> weekViewEvents = new ArrayList<>();

        ArrayList<Class> classes = ClassUtilsKt.getClasses(this);

        for (Class cls : classes) {
            Subject subject = SubjectUtilsKt.getSubjectFromId(this, cls.getSubjectId());

            ArrayList<ClassDetail> classDetails =
                    ClassUtilsKt.getClassDetailsFromIds(this, cls.getClassDetailIds());

            for (ClassDetail classDetail : classDetails) {
                ArrayList<ClassTime> classTimes =
                        ClassUtilsKt.getClassTimesFromIds(this, classDetail.getClassTimeIds());

                for (ClassTime classTime : classTimes) {
                    ArrayList<Integer> daysInMonth = getDaysInMonth(year, month, classTime.getDay());

                    for (int day : daysInMonth) {
                        int id = cls.getId();
                        String name = subject.getName();
                        String location = classDetail.getRoom();

                        Calendar startTime = new GregorianCalendar(
                                year,
                                month,
                                day,
                                classTime.getStartTime().getHour(),
                                classTime.getStartTime().getMinute());

                        Calendar endTime = new GregorianCalendar(
                                year,
                                month,
                                day,
                                classTime.getEndTime().getHour(),
                                classTime.getEndTime().getMinute());

                        WeekViewEvent event =
                                new WeekViewEvent(id, name, location, startTime, endTime);

                        Color color = new Color(subject.getColorId());
                        event.setColor(ContextCompat.getColor(
                                this, color.getPrimaryDarkColorResId(this)));

                        weekViewEvents.add(event);
                    }
                }
            }
        }

        return weekViewEvents;
    }

    private ArrayList<Integer> getDaysInMonth(int year, int month, DayOfWeek dayOfWeek) {
        // http://stackoverflow.com/a/3272519/4230345
        Calendar startOfMonth = new GregorianCalendar(year, month, 1);

        ArrayList<Integer> daysInMonth = new ArrayList<>();
        do {
            // get day of week for current day
            int day = startOfMonth.get(Calendar.DAY_OF_WEEK);
            if (day == dayOfWeek.getValue() + 1) {  // note +1 to make them correspond
                daysInMonth.add(startOfMonth.get(Calendar.DAY_OF_MONTH));
            }
            startOfMonth.add(Calendar.DAY_OF_YEAR, 1);  // advance to next day
        } while (startOfMonth.get(Calendar.MONTH) == month); // continue in this month

        return daysInMonth;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                populateLayout();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
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

    private void goToNow() {
        mWeekView.goToToday();
        mWeekView.goToHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 2);
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
