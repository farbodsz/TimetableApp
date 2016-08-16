package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.util.ClassUtilsKt;
import com.satsumasoftware.timetable.db.util.SubjectUtilsKt;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.ClassTimesAdapter;
import com.satsumasoftware.timetable.ui.adapter.SubjectsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ClassEditActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ClassDetailActivity";

    protected static final String EXTRA_CLASS = "extra_class";
    protected static final String EXTRA_LIST_POS = "extra_list_position";

    protected static final int REQUEST_CODE_SUBJECT_DETAIL = 2;
    protected static final int REQUEST_CODE_CLASS_TIME_DETAIL = 3;

    private boolean mIsNew;

    private int mNewDetailIdCount = 1;

    private Class mClass;
    private ArrayList<Integer> mClassDetailIds;

    private Subject mSubject;

    private TextView mSubjectText;
    private AlertDialog mSubjectDialog;

    private DynamicPagerAdapter mPagerAdapter;

    private ArrayList<ArrayList<ClassTime>> mClassTimes;
    private ArrayList<ClassTimesAdapter> mAdapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mClass = extras.getParcelable(EXTRA_CLASS);
        }
        mIsNew = mClass == null;

        int titleResId = mIsNew ? R.string.title_activity_class_new :
                R.string.title_activity_class_edit;
        getSupportActionBar().setTitle(getResources().getString(titleResId));

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCloseAction();
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        mPagerAdapter = new DynamicPagerAdapter();
        viewPager.setAdapter(mPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white));

        final ArrayList<Subject> subjects = SubjectUtilsKt.getSubjects(this);
        Collections.sort(subjects, new Comparator<Subject>() {
            @Override
            public int compare(Subject subject, Subject t1) {
                return subject.getName().compareTo(t1.getName());
            }
        });

        mSubjectText = (TextView) findViewById(R.id.textView_subject);
        mSubjectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClassEditActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View customView = inflater.inflate(R.layout.dialog_subjects, null);

                SubjectsAdapter adapter = new SubjectsAdapter(getBaseContext(), subjects);
                adapter.setOnEntryClickListener(new SubjectsAdapter.OnEntryClickListener() {
                    @Override
                    public void onEntryClick(View view, int position) {
                        mSubject = subjects.get(position);
                        mSubjectDialog.dismiss();
                        updateSubjectText();
                    }
                });

                RecyclerView recyclerView = (RecyclerView) customView.findViewById(R.id.recyclerView);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(ClassEditActivity.this));
                recyclerView.setAdapter(adapter);

                Button button = (Button) customView.findViewById(R.id.button_new);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ClassEditActivity.this, SubjectEditActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL);
                    }
                });

                builder.setView(customView);

                mSubjectDialog = builder.create();
                mSubjectDialog.show();
            }
        });

        mClassDetailIds = new ArrayList<>();

        mClassTimes = new ArrayList<>();
        mAdapters = new ArrayList<>();

        if (!mIsNew) {
            mSubject = SubjectUtilsKt.getSubjectFromId(this, mClass.getSubjectId());
            updateSubjectText();

            ArrayList<ClassDetail> classDetails =
                    ClassUtilsKt.getClassDetailsFromIds(this, mClass.getClassDetailIds());
            for (ClassDetail classDetail : classDetails) {
                addDetailTab(classDetail, false);
            }
        } else {
            addDetailTab(null, false);  // first tab for adding detail
        }
        addDetailTab(null, true);
    }

    private void updateSubjectText() {
        mSubjectText.setText(mSubject.getName());
        mSubjectText.setTextColor(ContextCompat.getColor(
                ClassEditActivity.this, R.color.mdu_text_white));
    }

    private void addDetailTab(ClassDetail classDetail, boolean placeHolder) {
        boolean isNewDetail = classDetail == null;

        // MUST be final so that it doesn't change as adapter count updates
        final int pagerCount = mPagerAdapter.getCount();

        final int classDetailId = isNewDetail ?
                ClassUtilsKt.getHighestClassDetailId(this) + mNewDetailIdCount :
                classDetail.getId();
        mClassDetailIds.add(classDetailId);

        View page = getLayoutInflater().inflate(R.layout.fragment_class_edit, null);

        EditText room = (EditText) page.findViewById(R.id.editText_room);
        if (!isNewDetail) {
            room.setText(classDetail.getRoom());
        }

        EditText teacher = (EditText) page.findViewById(R.id.editText_teacher);
        if (!isNewDetail) {
            teacher.setText(classDetail.getTeacher());
        }

        final ArrayList<ClassTime> classTimes = isNewDetail ? new ArrayList<ClassTime>() :
                ClassUtilsKt.getClassTimesFromIds(this, classDetail.getClassTimeIds());
        mClassTimes.add(classTimes);

        ClassTimesAdapter adapter = new ClassTimesAdapter(classTimes);
        adapter.setOnEntryClickListener(new ClassTimesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(ClassEditActivity.this, ClassTimeEditActivity.class);
                intent.putExtra(ClassTimeEditActivity.EXTRA_CLASS_TIME, classTimes.get(position));
                intent.putExtra(ClassTimeEditActivity.EXTRA_CLASS_DETAIL_ID, classDetailId);
                intent.putExtra(ClassTimeEditActivity.EXTRA_TAB_POSITION, pagerCount);
                intent.putExtra(ClassTimeEditActivity.EXTRA_LIST_POS, position);
                startActivityForResult(intent, REQUEST_CODE_CLASS_TIME_DETAIL);
            }
        });
        mAdapters.add(adapter);

        RecyclerView recyclerView = (RecyclerView) page.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        recyclerView.setAdapter(adapter);

        Button btnAddTime = (Button) page.findViewById(R.id.button_add_time);
        btnAddTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassEditActivity.this, ClassTimeEditActivity.class);
                intent.putExtra(ClassTimeEditActivity.EXTRA_CLASS_DETAIL_ID, classDetailId);
                intent.putExtra(ClassTimeEditActivity.EXTRA_TAB_POSITION, pagerCount);
                startActivityForResult(intent, REQUEST_CODE_CLASS_TIME_DETAIL);
            }
        });

        if (placeHolder) {
            final ViewGroup content = (ViewGroup) page.findViewById(R.id.content);
            content.setVisibility(View.GONE);

            final Button btnAddTab = (Button) page.findViewById(R.id.button_add_tab);
            btnAddTab.setVisibility(View.VISIBLE);
            btnAddTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    content.setVisibility(View.VISIBLE);
                    btnAddTab.setVisibility(View.GONE);
                    addDetailTab(null, true);
                }
            });
        }

        mPagerAdapter.addViewWithTitle(page, "Detail " + (pagerCount + 1));

        if (isNewDetail) {
            mNewDetailIdCount++;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SUBJECT_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                mSubject = data.getParcelableExtra(SubjectEditActivity.EXTRA_SUBJECT);
                mSubjectDialog.dismiss();
                updateSubjectText();
            }

        } else if (requestCode == REQUEST_CODE_CLASS_TIME_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                ClassTime classTime = data.getParcelableExtra(ClassTimeEditActivity.EXTRA_CLASS_TIME);
                int tabIndex = data.getIntExtra(ClassTimeEditActivity.EXTRA_TAB_POSITION, -1);
                int listPos = data.getIntExtra(ClassTimeEditActivity.EXTRA_LIST_POS, -1);
                @ClassTimeEditActivity.Action int actionType =
                        data.getIntExtra(ClassTimeEditActivity.EXTRA_RESULT_ACTION, -1);

                Log.d(LOG_TAG, "Refreshing adapter at tab index " + tabIndex);

                ArrayList<ClassTime> someTimes = mClassTimes.get(tabIndex);
                switch (actionType) {
                    case ClassTimeEditActivity.ACTION_NEW:
                        someTimes.add(classTime);
                        ClassUtilsKt.addClassTime(this, classTime);
                        break;
                    case ClassTimeEditActivity.ACTION_EDIT:
                        someTimes.set(listPos, classTime);
                        ClassUtilsKt.replaceClassTime(this, classTime.getId(), classTime);
                        break;
                    case ClassTimeEditActivity.ACTION_DELETE:
                        someTimes.remove(listPos);
                        ClassUtilsKt.completelyDeleteClassTime(this, classTime.getId());
                        break;
                }
                mAdapters.get(tabIndex).notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mIsNew) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                handleDoneAction();
                break;
            case R.id.action_delete:
                handleDeleteAction();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        handleCloseAction();
        super.onBackPressed();
    }

    private void handleCloseAction() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void handleDoneAction() {
        // validate subject

        if (mSubject == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_subject_required,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        // go through each page and only collect data first - so any validation
        // errors can be resolved without any data being written or saved

        ArrayList<View> pages = mPagerAdapter.getAllViews();

        ArrayList<Integer> classDetailIds = new ArrayList<>();
        ArrayList<String> rooms = new ArrayList<>();
        ArrayList<String> teachers = new ArrayList<>();
        ArrayList<ArrayList<Integer>> classTimeIdsList = new ArrayList<>();

        // note the - 1 from .size() to exclude the placeholder tab
        for (int i = 0; i < pages.size() - 1; i++) {
            Log.d(LOG_TAG, "Collecting values at tab index " + i);

            View page = pages.get(i);

            int classDetailId = mClassDetailIds.get(i);

            EditText roomText = (EditText) page.findViewById(R.id.editText_room);
            String room = roomText.getText().toString();
            Log.d(LOG_TAG, "room: " + room);

            EditText teacherText = (EditText) page.findViewById(R.id.editText_teacher);
            String teacher = teacherText.getText().toString();
            Log.d(LOG_TAG, "teacher: " + teacher);

            ArrayList<ClassTime> classTimes = mClassTimes.get(i);
            if (classTimes.isEmpty()) {
                Log.d(LOG_TAG, "class times list is empty!");
                if (room.trim().equals("") && teacher.trim().equals("")) {
                    // this is an empty detail page: room, teacher and times are empty
                    Snackbar.make(findViewById(R.id.rootView),
                            R.string.message_empty_detail, Snackbar.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "completely empty detail page");
                    return;
                } else {
                    // this has a room or teacher but not times (which it needs)
                    Snackbar.make(findViewById(R.id.rootView),
                            R.string.message_missing_time_for_detail, Snackbar.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "room and/or teacher, but no times");
                    return;
                }
            }

            classDetailIds.add(classDetailId);
            rooms.add(room);
            teachers.add(teacher);

            ArrayList<Integer> arrayList = new ArrayList<>();
            for (ClassTime classTime : classTimes) arrayList.add(classTime.getId());
            classTimeIdsList.add(arrayList);
        }

        if (rooms.size() == 0) {
            // if nothing has been added
            Log.d(LOG_TAG, "nothing entered");
            handleCloseAction();
            return;
        }

        // now write the data (replace class detail values)

        int classId = mIsNew ? ClassUtilsKt.getHighestClassId(this) + 1 : mClass.getId();

        for (int i = 0; i < rooms.size(); i++) {
            int classDetailId = classDetailIds.get(i);
            String room = rooms.get(i);
            String teacher = teachers.get(i);
            ArrayList<Integer> classTimeIds = classTimeIdsList.get(i);

            ClassDetail classDetail =
                    new ClassDetail(classDetailId, classId, room, teacher, classTimeIds);

            ClassUtilsKt.replaceClassDetail(this, classDetailId, classDetail);
        }

        mClass = new Class(classId, mSubject.getId(), classDetailIds);

        if (mIsNew) {
            ClassUtilsKt.addClass(this, mClass);
        } else {
            ClassUtilsKt.replaceClass(this, mClass.getId(), mClass);
        }

        setResult(Activity.RESULT_OK);
        finish();
    }

    private void handleDeleteAction() {
        ClassUtilsKt.completelyDeleteClass(this, mClass);
        setResult(Activity.RESULT_OK);
        finish();
    }


}
