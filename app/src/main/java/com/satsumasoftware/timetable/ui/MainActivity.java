package com.satsumasoftware.timetable.ui;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.TimetableApplication;
import com.satsumasoftware.timetable.db.AssignmentUtils;
import com.satsumasoftware.timetable.db.ClassTimeUtils;
import com.satsumasoftware.timetable.db.ExamUtils;
import com.satsumasoftware.timetable.framework.Assignment;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.framework.Timetable;
import com.satsumasoftware.timetable.ui.adapter.HomeCardsAdapter;
import com.satsumasoftware.timetable.ui.card.AssignmentsCard;
import com.satsumasoftware.timetable.ui.card.ClassesCard;
import com.satsumasoftware.timetable.ui.card.ExamsCard;
import com.satsumasoftware.timetable.ui.card.HomeCard;
import com.satsumasoftware.timetable.util.DateUtils;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * The main screen showing an overview of the user's classes, assignments and exams.
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Timetable currentTimetable = ((TimetableApplication) getApplication()).getCurrentTimetable();
        assert currentTimetable != null;

        assert getSupportActionBar() != null;
        getSupportActionBar().setSubtitle(currentTimetable.getDisplayedName());

        setupLayout();
    }

    private void setupLayout() {
        TextView infoBar = (TextView) findViewById(R.id.text_infoBar);
        infoBar.setVisibility(View.VISIBLE);

        String todayText = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d MMMM yyyy"));
        infoBar.setText(todayText);

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
        LocalDate now = LocalDate.now();
        DayOfWeek today = now.getDayOfWeek();
        int weekNumber = DateUtils.findWeekNumber(getApplication());

        ArrayList<ClassTime> classTimes =
                ClassTimeUtils.getClassTimesForDay(this, today, weekNumber, now);

        Collections.sort(classTimes, new Comparator<ClassTime>() {
            @Override
            public int compare(ClassTime ct1, ClassTime ct2) {
                return ct1.getStartTime().compareTo(ct2.getStartTime());
            }
        });

        return classTimes;
    }

    private ArrayList<Assignment> getAssignments() {
        ArrayList<Assignment> assignments = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (Assignment assignment : new AssignmentUtils(this).getItems(getApplication())) {
            LocalDate dueDate = assignment.getDueDate();

            boolean dueInNextThreeDays = dueDate.isAfter(now) && dueDate.isBefore(now.plusDays(4));

            if (assignment.isOverdue() || dueDate.isEqual(now) || dueInNextThreeDays) {
                assignments.add(assignment);  // overdue (incomplete) assignment
            }
        }

        Collections.sort(assignments, new Comparator<Assignment>() {
            @Override
            public int compare(Assignment a1, Assignment a2) {
                LocalDate date1 = a1.getDueDate();
                LocalDate date2 = a2.getDueDate();
                return date1.compareTo(date2);
            }
        });

        return assignments;
    }

    private ArrayList<Exam> getExams() {
        ArrayList<Exam> exams = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Exam exam : new ExamUtils(this).getItems(getApplication())) {
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
