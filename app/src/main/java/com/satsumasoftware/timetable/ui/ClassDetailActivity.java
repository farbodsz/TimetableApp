package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.ClassesUtils;
import com.satsumasoftware.timetable.db.SubjectsUtils;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.framework.ClassTime;
import com.satsumasoftware.timetable.framework.Subject;
import com.satsumasoftware.timetable.ui.adapter.ClassTimesAdapter;
import com.satsuware.usefulviews.LabelledSpinner;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class ClassDetailActivity extends AppCompatActivity {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_NEW, ACTION_EDIT, ACTION_DELETE})
    public @interface Action {}
    public static final int ACTION_NEW = 0;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_DELETE = 2;

    protected static final String EXTRA_CLASS = "extra_class";
    protected static final String EXTRA_LIST_POS = "extra_list_position";
    protected static final String EXTRA_RESULT_ACTION = "extra_result_action";

    protected static final int REQUEST_CODE_CLASS_TIME_DETAIL = 2;

    private boolean mIsNew;

    private int mNewDetailIdCount = 1;

    private Class mClass;
    private ArrayList<Integer> mClassDetailIds;

    private int mListPosition = SubjectsActivity.LIST_POS_INVALID;

    private Subject mSubject;
    private LabelledSpinner mSpinner;
    private ClassDetailPagerAdapter mPagerAdapter;

    private ArrayList<ArrayList<ClassTime>> mClassTimes;
    private ArrayList<ClassTimesAdapter> mAdapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mClass = extras.getParcelable(EXTRA_CLASS);
            mListPosition = extras.getInt(EXTRA_LIST_POS);
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
        mPagerAdapter = new ClassDetailPagerAdapter();
        viewPager.setAdapter(mPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white));

        final ArrayList<Subject> subjects = SubjectsUtils.getSubjects(this);
        Collections.sort(subjects, new Comparator<Subject>() {
            @Override
            public int compare(Subject subject, Subject t1) {
                return subject.getName().compareTo(t1.getName());
            }
        });

        ArrayList<String> subjectNames = getSubjectNames(subjects);

        mSpinner = (LabelledSpinner) findViewById(R.id.spinner_subject);
        mSpinner.setItemsArray(subjectNames);
        mSpinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
                mSubject = subjects.get(position);
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {}
        });

        mClassDetailIds = new ArrayList<>();

        mClassTimes = new ArrayList<>();
        mAdapters = new ArrayList<>();

        if (!mIsNew) {
            Subject subject = SubjectsUtils.getSubjectFromId(this, mClass.getSubjectId());
            int pos = subjectNames.indexOf(subject.getName());
            mSpinner.setSelection(pos);

            ArrayList<ClassDetail> classDetails =
                    ClassesUtils.getClassDetailsFromIds(this, mClass.getClassDetailIds());
            for (ClassDetail classDetail : classDetails) {
                addDetailTab(classDetail, false);
            }
        } else {
            addDetailTab(null, false);  // first tab for adding detail
        }
        addDetailTab(null, true);
    }

    private ArrayList<String> getSubjectNames(ArrayList<Subject> subjects) {
        ArrayList<String> names = new ArrayList<>();
        for (Subject subject : subjects) {
            names.add(subject.getName());
        }
        return names;
    }

    private void addDetailTab(ClassDetail classDetail, boolean placeHolder) {
        boolean isNewDetail = classDetail == null;

        // MUST be final so that it doesn't change as adapter count updates
        final int pagerCount = mPagerAdapter.getCount();

        mClassDetailIds.add(isNewDetail ?
                ClassesUtils.getHighestClassDetailId(this) + mNewDetailIdCount :
                classDetail.getId());

        View page = getLayoutInflater().inflate(R.layout.fragment_class_detail, null);

        EditText room = (EditText) page.findViewById(R.id.editText_room);
        if (!isNewDetail) {
            room.setText(classDetail.getRoom());
        }

        EditText teacher = (EditText) page.findViewById(R.id.editText_teacher);
        if (!isNewDetail) {
            teacher.setText(classDetail.getTeacher());
        }

        final ArrayList<ClassTime> classTimes = isNewDetail ? new ArrayList<ClassTime>() :
                ClassesUtils.getClassTimesFromIds(this, classDetail.getClassTimeIds());
        mClassTimes.add(classTimes);

        ClassTimesAdapter adapter = new ClassTimesAdapter(classTimes);
        adapter.setOnEntryClickListener(new ClassTimesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(ClassDetailActivity.this, ClassTimeDetailActivity.class);
                intent.putExtra(ClassTimeDetailActivity.EXTRA_CLASS_TIME, classTimes.get(position));
                intent.putExtra(ClassTimeDetailActivity.EXTRA_TAB_POSITION, pagerCount);
                intent.putExtra(ClassTimeDetailActivity.EXTRA_LIST_POS, position);
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
                Intent intent = new Intent(ClassDetailActivity.this, ClassTimeDetailActivity.class);
                intent.putExtra(ClassTimeDetailActivity.EXTRA_TAB_POSITION, pagerCount);
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

        if (requestCode == REQUEST_CODE_CLASS_TIME_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                ClassTime classTime = data.getParcelableExtra(ClassTimeDetailActivity.EXTRA_CLASS_TIME);
                int tabIndex = data.getIntExtra(ClassTimeDetailActivity.EXTRA_TAB_POSITION, -1);
                int listPos = data.getIntExtra(ClassTimeDetailActivity.EXTRA_LIST_POS, -1);
                @ClassTimeDetailActivity.Action int actionType =
                        data.getIntExtra(ClassTimeDetailActivity.EXTRA_RESULT_ACTION, -1);

                Log.d("TAB INDEX", "is : " + tabIndex);

                ArrayList<ClassTime> someTimes = mClassTimes.get(tabIndex);
                switch (actionType) {
                    case ClassTimeDetailActivity.ACTION_NEW:
                        someTimes.add(classTime);
                        ClassesUtils.addClassTime(this, classTime);
                        break;
                    case ClassTimeDetailActivity.ACTION_EDIT:
                        someTimes.set(listPos, classTime);
                        ClassesUtils.replaceClassTime(this, classTime.getId(), classTime);
                        break;
                    case ClassTimeDetailActivity.ACTION_DELETE:
                        someTimes.remove(listPos);
                        ClassesUtils.completelyDeleteClassTime(this, classTime.getId());
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
            Log.d("CDA", "---");

            View page = pages.get(i);

            int classDetailId = mClassDetailIds.get(i);

            EditText roomText = (EditText) page.findViewById(R.id.editText_room);
            String room = roomText.getText().toString();
            Log.d("CDA", "room: " + room);

            EditText teacherText = (EditText) page.findViewById(R.id.editText_teacher);
            String teacher = teacherText.getText().toString();
            Log.d("CDA", "teacher: " + teacher);

            ArrayList<ClassTime> classTimes = mClassTimes.get(i);
            if (classTimes.isEmpty()) {
                Log.d("CDA", "class times list is empty");
                if (room.trim().equals("") && teacher.trim().equals("")) {
                    // this is an empty detail page: room, teacher and times are empty
                    Snackbar.make(findViewById(R.id.rootView),
                            R.string.message_empty_detail, Snackbar.LENGTH_SHORT).show();
                    Log.d("CDA", "completely empty detail page");
                    return;
                } else {
                    // this has a room or teacher but not times (which it needs)
                    Snackbar.make(findViewById(R.id.rootView),
                            R.string.message_missing_time_for_detail, Snackbar.LENGTH_SHORT).show();
                    Log.d("CDA", "room and teacher, but no times");
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
            Log.d("CDA", "nothing entered");
            handleCloseAction();
            return;
        }

        // now write the data (replace class detail values)

        for (int i = 0; i < rooms.size(); i++) {
            int classDetailId = classDetailIds.get(i);
            String room = rooms.get(i);
            String teacher = teachers.get(i);
            ArrayList<Integer> classTimeIds = classTimeIdsList.get(i);

            ClassDetail classDetail = new ClassDetail(classDetailId, room, teacher, classTimeIds);

            ClassesUtils.replaceClassDetail(this, classDetailId, classDetail);
            ClassesUtils.replaceClassDetailToTimesLinks(this, classDetailId, classTimeIds);
        }

        int id = mIsNew ? ClassesUtils.getHighestClassId(this) + 1 : mClass.getId();
        mClass = new Class(id, mSubject.getId(), classDetailIds);

        if (mIsNew) {
            ClassesUtils.addClass(this, mClass);
            ClassesUtils.addClassToDetailsLinks(this, mClass.getId(), mClass.getClassDetailIds());
        } else {
            ClassesUtils.replaceClass(this, mClass.getId(), mClass);
            ClassesUtils.replaceClassToDetailsLinks(this, mClass.getId(), mClass.getClassDetailIds());
        }

        setResult(Activity.RESULT_OK);
        finish();
    }

    private void handleDeleteAction() {
        ClassesUtils.completelyDeleteClass(this, mClass);
        setResult(Activity.RESULT_OK);
        finish();
    }


    public class ClassDetailPagerAdapter extends PagerAdapter {

        private ArrayList<View> mViews = new ArrayList<>();
        private ArrayList<String> mTitles = new ArrayList<>();

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public int getItemPosition(Object object) {
            int index = mViews.indexOf(object);
            if (index == -1) {
                return POSITION_NONE;
            } else {
                return index;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            return mTitles.get(position).toUpperCase(l);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = mViews.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViews.get(position));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public int addView(View view) {
            return addView(view, mViews.size());
        }

        public int addViewWithTitle(View view, String title) {
            return addViewWithTitle(view, mViews.size(), title);
        }

        public int addView(View view, int position) {
            return addViewWithTitle(view, position, "");
        }

        public int addViewWithTitle(View view, int position, String title) {
            mViews.add(position, view);
            mTitles.add(title);
            notifyDataSetChanged();
            return position;
        }

        public int removeView(ViewPager pager, View view) {
            return removeView(pager, mViews.indexOf(view));
        }

        public int removeView(ViewPager pager, int position) {
            pager.setAdapter(null);
            mViews.remove(position);
            pager.setAdapter(this);
            return position;
        }

        public View getView(int position) {
            return mViews.get(position);
        }

        public ArrayList<View> getAllViews() {
            return mViews;
        }
    }

}
