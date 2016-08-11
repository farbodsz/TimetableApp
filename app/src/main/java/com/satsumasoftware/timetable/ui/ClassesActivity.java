package com.satsumasoftware.timetable.ui;

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
import com.satsumasoftware.timetable.db.TimetableDbHelper;
import com.satsumasoftware.timetable.framework.Class;
import com.satsumasoftware.timetable.ui.adapter.ClassesAdapter;

import java.util.ArrayList;

public class ClassesActivity extends BaseActivity {

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
                // TODO
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });
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
