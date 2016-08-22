package com.satsumasoftware.timetable.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.ClassTimesSchema;
import com.satsumasoftware.timetable.db.TimetableDbHelper;
import com.satsumasoftware.timetable.db.util.AssignmentUtilsKt;
import com.satsumasoftware.timetable.db.util.ExamUtilsKt;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.ui.adapter.HomeCardsAdapter;
import com.satsumasoftware.timetable.ui.card.AssignmentsCard;
import com.satsumasoftware.timetable.ui.card.ClassesCard;
import com.satsumasoftware.timetable.ui.card.ExamsCard;
import com.satsumasoftware.timetable.ui.card.HomeCard;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<HomeCard> cards = new ArrayList<>();

        cards.add(new ClassesCard(this, getClassesToday()));
        cards.add(new AssignmentsCard(this, getAssignments()));

        ArrayList<Exam> exams = getExams();
        if (!exams.isEmpty()) {
            cards.add(new ExamsCard(this, getExams()));
        }

        recyclerView.setAdapter(new HomeCardsAdapter(this, cards));
    }

    private ArrayList<ClassTime> getClassesToday() {
        ArrayList<ClassTime> classTimes = new ArrayList<>();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(this);
        Cursor cursor = dbHelper.getReadableDatabase().query(
                ClassTimesSchema.TABLE_NAME,
                null,
                ClassTimesSchema.COL_DAY + "=?",
                new String[] {String.valueOf(today.getValue())},
                null,
                null,
                ClassTimesSchema.COL_START_TIME_HRS);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            classTimes.add(new ClassTime(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return classTimes;
    }

    private ArrayList<Assignment> getAssignments() {
        ArrayList<Assignment> assignments = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (Assignment assignment : AssignmentUtilsKt.getAssignments(this)) {
            LocalDate dueDate = assignment.getDueDate();

            if (dueDate.isBefore(now) && assignment.getCompletionProgress() != 100) {
                assignments.add(assignment);  // overdue (incomplete) assignment
                continue;
            }

            if (dueDate.isEqual(now)) {
                assignments.add(assignment);  // due today
                continue;
            }

            if (dueDate.isAfter(now) && dueDate.isBefore(now.plusDays(4))) {
                assignments.add(assignment);  // due in the next three days
            }
        }

        // sort
        Collections.sort(assignments, new Comparator<Assignment>() {
            @Override
            public int compare(Assignment assignment, Assignment t1) {
                LocalDate date1 = assignment.getDueDate();
                LocalDate date2 = t1.getDueDate();
                return date1.compareTo(date2);
            }
        });

        return assignments;
    }

    private ArrayList<Exam> getExams() {
        ArrayList<Exam> exams = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Exam exam : ExamUtilsKt.getExams(this)) {
            LocalDateTime examDateTime = exam.makeDateTimeObject();

            if (!examDateTime.isBefore(now) && examDateTime.isBefore(now.plusWeeks(6))) {
                exams.add(exam);
            }
        }

        Collections.sort(exams, new Comparator<Exam>() {
            @Override
            public int compare(Exam exam1, Exam exam2) {
                LocalDateTime dateTime1 = exam1.makeDateTimeObject();
                LocalDateTime dateTime2 = exam2.makeDateTimeObject();
                return dateTime1.compareTo(dateTime2);
            }
        });

        return exams;
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
        return NAVDRAWER_ITEM_HOME;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
