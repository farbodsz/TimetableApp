package com.satsumasoftware.timetable.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.satsumasoftware.timetable.R;
import com.satsumasoftware.timetable.db.ClassesSchema;
import com.satsumasoftware.timetable.db.ClassesUtils;
import com.satsumasoftware.timetable.db.TimetableDbHelper;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.framework.ClassDetail;
import com.satsumasoftware.timetable.ui.adapter.ClassesAdapter;

import java.util.ArrayList;

public class ClassesActivity extends BaseActivity {

    protected static final int REQUEST_CODE_CLASS_DETAIL = 1;
    protected static final int LIST_POS_INVALID = -1;

    private ArrayList<Class> mClasses;
    private ClassesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mClasses = new ArrayList<>();
        TimetableDbHelper dbHelper = TimetableDbHelper.getInstance(this);
        Cursor cursor = dbHelper.getReadableDatabase().query(
                ClassesSchema.TABLE_NAME, null, null, null, null, null, null);
        while (!cursor.isAfterLast()) {
            mClasses.add(new Class(this, cursor));
            cursor.moveToNext();
        }
        cursor.close();

        mAdapter = new ClassesAdapter(this, mClasses);
        mAdapter.setOnEntryClickListener(new ClassesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(ClassesActivity.this, ClassDetailActivity.class);
                intent.putExtra(ClassDetailActivity.EXTRA_CLASS, mClasses.get(position));
                intent.putExtra(ClassDetailActivity.EXTRA_LIST_POS, position);
                startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassesActivity.this, ClassDetailActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                Class cls = data.getParcelableExtra(ClassDetailActivity.EXTRA_CLASS);
                int listPos = data.getIntExtra(ClassDetailActivity.EXTRA_LIST_POS, -1);
                @ClassDetailActivity.Action int actionType =
                        data.getIntExtra(ClassDetailActivity.EXTRA_RESULT_ACTION, -1);

                switch (actionType) {
                    case ClassDetailActivity.ACTION_NEW:
                        mClasses.add(cls);
                        ClassesUtils.addClass(this, cls);
                        ClassesUtils.addClassToDetailsLinks(this, cls.getId(), cls.getClassDetailIds());
                        break;
                    case ClassDetailActivity.ACTION_EDIT:
                        mClasses.set(listPos, cls);
                        ClassesUtils.replaceClass(this, cls.getId(), cls);
                        break;
                    case ClassDetailActivity.ACTION_DELETE:
                        // TODO
                        break;
                }
                mAdapter.notifyDataSetChanged();
            }
        }
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
        return NAVDRAWER_ITEM_CLASSES;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
