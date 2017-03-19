package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.data.handler.ExamHandler;
import com.satsumasoftware.timetable.data.handler.TimetableItemHandler;
import com.satsumasoftware.timetable.framework.Exam;
import com.satsumasoftware.timetable.ui.adapter.ExamsAdapter;
import com.satsumasoftware.timetable.util.DateUtils;
import com.satsumasoftware.timetable.util.UiUtils;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * An activity for displaying a list of exams to the user.
 *
 * If there are no classes to display, a placeholder background will be shown instead.
 *
 * Clicking on a class will allow the user to view its details in {@link ExamDetailActivity}.
 * The user can also choose to create a new exam in which case {@link ExamDetailActivity}
 * will also be invoked but with no intent extra data.
 *
 * @see Exam
 * @see ExamDetailActivity
 * @see ExamEditActivity
 */
public class ExamsActivity extends ItemListActivity<Exam> {

    private static final int REQUEST_CODE_EXAM_EDIT = 1;

    private ArrayList<String> mHeaders;

    private boolean mShowPast;

    @Override
    TimetableItemHandler<Exam> instantiateDataHandler() {
        return new ExamHandler(this);
    }

    @Override
    void onFabButtonClick() {
        Intent intent = new Intent(ExamsActivity.this, ExamEditActivity.class);
        startActivityForResult(intent, REQUEST_CODE_EXAM_EDIT);
    }

    @Override
    RecyclerView.Adapter setupAdapter() {
        ExamsAdapter adapter = new ExamsAdapter(this, mHeaders, mItems);

        adapter.setOnEntryClickListener(new ExamsAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(ExamsActivity.this, ExamDetailActivity.class);
                intent.putExtra(ExamDetailActivity.EXTRA_ITEM, mItems.get(position));

                Bundle bundle = null;
                if (UiUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    ExamsActivity.this,
                                    view,
                                    getString(R.string.transition_1));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        ExamsActivity.this, intent, REQUEST_CODE_EXAM_EDIT, bundle);
            }
        });

        return adapter;
    }

    @Override
    void setupList() {
        mHeaders = new ArrayList<>();
        super.setupList();
    }

    @Override
    void sortList() {
        Collections.sort(mItems, new Comparator<Exam>() {
            @Override
            public int compare(Exam e1, Exam e2) {
                LocalDateTime dateTime1 = e1.makeDateTimeObject();
                LocalDateTime dateTime2 = e2.makeDateTimeObject();
                if (mShowPast) {
                    return dateTime2.compareTo(dateTime1);
                } else {
                    return dateTime1.compareTo(dateTime2);
                }
            }
        });

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<Exam> exams = new ArrayList<>();

        int currentTimePeriod = -1;

        for (int i = 0; i < mItems.size(); i++) {
            Exam exam = mItems.get(i);

            LocalDate examDate = exam.getDate();
            int timePeriodId;

            if (exam.makeDateTimeObject().isBefore(LocalDateTime.now())) {
                if (mShowPast) {
                    timePeriodId = Integer.parseInt(String.valueOf(examDate.getYear()) +
                            String.valueOf(examDate.getMonthValue()));

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(examDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")));
                        exams.add(null);
                    }

                    headers.add(null);
                    exams.add(exam);

                    currentTimePeriod = timePeriodId;
                }

            } else {

                if (!mShowPast) {
                    timePeriodId = DateUtils.getDatePeriodId(examDate);

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(DateUtils.makeHeaderName(this, timePeriodId));
                        exams.add(null);
                    }

                    headers.add(null);
                    exams.add(exam);

                    currentTimePeriod = timePeriodId;
                }
            }
        }

        mHeaders.clear();
        mHeaders.addAll(headers);

        mItems.clear();
        mItems.addAll(exams);
    }

    @Override
    View getPlaceholderView() {
        int stringRes = mShowPast ? R.string.placeholder_exams_past :
                R.string.placeholder_exams;

        return UiUtils.makePlaceholderView(this,
                R.drawable.ic_assessment_black_24dp,
                stringRes,
                R.color.mdu_blue_400,
                R.color.mdu_white,
                R.color.mdu_white,
                true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EXAM_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_assignments, menu);
        menu.findItem(R.id.action_show_past).setTitle(getString(R.string.action_show_past_exams));
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
                    textView.setText(getString(R.string.showing_past_exams));
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
        return NAVDRAWER_ITEM_EXAMS;
    }

}
