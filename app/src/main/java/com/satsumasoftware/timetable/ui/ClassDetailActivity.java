package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

    private boolean mIsNew;

    private Class mClass;
    private int mListPosition = SubjectsActivity.LIST_POS_INVALID;

    private LabelledSpinner mSpinner;
    private ClassDetailPagerAdapter mPagerAdapter;

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

        ArrayList<Subject> subjects = SubjectsUtils.getSubjects(this);
        Collections.sort(subjects, new Comparator<Subject>() {
            @Override
            public int compare(Subject subject, Subject t1) {
                return subject.getName().compareTo(t1.getName());
            }
        });

        ArrayList<String> subjectNames = getSubjectNames(subjects);

        mSpinner = (LabelledSpinner) findViewById(R.id.spinner_subject);
        mSpinner.setItemsArray(subjectNames);

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

        View page = getLayoutInflater().inflate(R.layout.fragment_class_detail, null);

        EditText room = (EditText) page.findViewById(R.id.editText_room);
        if (!isNewDetail) {
            room.setText(classDetail.getRoom());
        }

        EditText teacher = (EditText) page.findViewById(R.id.editText_teacher);
        if (!isNewDetail) {
            teacher.setText(classDetail.getTeacher());
        }

        ArrayList<ClassTime> classTimes = isNewDetail ? new ArrayList<ClassTime>() :
                ClassesUtils.getClassTimesFromIds(this, classDetail.getClassTimeIds());

        ClassTimesAdapter adapter = new ClassTimesAdapter(classTimes);
        adapter.setOnEntryClickListener(new ClassTimesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                // TODO
            }
        });

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
                // TODO
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

        mPagerAdapter.addViewWithTitle(page, "Detail");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_subject_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                //handleDoneAction();
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

    /*

    private void handleDoneAction() {
        String newName = mEditText.getText().toString();
        if (newName.length() == 0) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_invalid_name,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        newName = TextUtilsKt.title(newName);

        @Action int actionType;

        if (mIsNew) {
            mClass = new Subject(DatabaseUtils.getHighestSubjectId(this) + 1, newName);
            actionType = ACTION_NEW;
        } else {
            mClass.setName(newName);
            actionType = ACTION_EDIT;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_CLASS, mClass);
        intent.putExtra(EXTRA_LIST_POS, mListPosition);
        intent.putExtra(EXTRA_RESULT_ACTION, actionType);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void handleDeleteAction() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CLASS, mClass);
        intent.putExtra(EXTRA_LIST_POS, mListPosition);
        intent.putExtra(EXTRA_RESULT_ACTION, ACTION_DELETE);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    */


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
    }

}
