package com.satsumasoftware.timetable.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.satsumasoftware.timetable.R;

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * This method should be overridden in subclasses of BaseActivity to use the Toolbar
     * Return null if there is no Toolbar
     */
    protected Toolbar getSelfToolbar() {
        return null;
    }

    /**
     * This method should be overridden in subclasses of BaseActivity to use the DrawerLayout
     * Return null if there is no DrawerLayout
     */
    protected DrawerLayout getSelfDrawerLayout() {
        return null;
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    /**
     * Returns the NavigationView that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return null to mean that this Activity should not have a Nav Drawer.
     */
    protected NavigationView getSelfNavigationView() {
        return null;
    }

    /*
     * This is a list of all items in the nav drawer and their corresponding menu ids
     */
    protected static final int NAVDRAWER_ITEM_HOME = R.id.navigation_item_home;
    protected static final int NAVDRAWER_ITEM_SCHEDULE = R.id.navigation_item_schedule;
    protected static final int NAVDRAWER_ITEM_CLASSES = R.id.navigation_item_classes;
    protected static final int NAVDRAWER_ITEM_TODO = R.id.navigation_item_todo;
    protected static final int NAVDRAWER_ITEM_ASSIGNMENTS = R.id.navigation_item_assignments;
    protected static final int NAVDRAWER_ITEM_EXAMS = R.id.navigation_item_exams;
    protected static final int NAVDRAWER_ITEM_SUBJECTS = R.id.navigation_item_subjects;
    protected static final int NAVDRAWER_ITEM_MANAGE_TIMETABLES = R.id.navigation_item_manage_timetables;
    protected static final int NAVDRAWER_ITEM_SETTINGS = R.id.navigation_item_settings;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;

    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;


    /*
     * Methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerLayout = getSelfDrawerLayout();
        if (mDrawerLayout == null) {
            return;
        }

        setupLayout();
    }

    private void setupLayout() {
        Toolbar toolbar = getSelfToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mNavigationView = getSelfNavigationView();
        if (mNavigationView == null) {
            return;
        }
        mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(true);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                handleNavigationSelection(menuItem);
                return true;
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();
    }

    private void handleNavigationSelection(final MenuItem menuItem) {
        if (menuItem.getItemId() == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawers();
            return;
        }

        // launch the target Activity after a short delay, to allow the close animation to play
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNavDrawerItem(menuItem.getItemId());
            }
        }, NAVDRAWER_LAUNCH_DELAY);

        if (menuItem.isCheckable()) {
            mNavigationView.getMenu().findItem(getSelfNavDrawerItem()).setChecked(false);
            menuItem.setChecked(true);
        }

        mDrawerLayout.closeDrawers();
    }

    private void goToNavDrawerItem(int menuItem) {
        Intent intent;
        switch (menuItem) {
            case NAVDRAWER_ITEM_HOME:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case NAVDRAWER_ITEM_SCHEDULE:
                intent = new Intent(this, ScheduleActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case NAVDRAWER_ITEM_CLASSES:
                intent = new Intent(this, ClassesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case NAVDRAWER_ITEM_TODO:
                intent = new Intent(this, AssignmentsActivity.class);
                intent.putExtra(AssignmentsActivity.EXTRA_MODE, AssignmentsActivity.DISPLAY_TODO);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case NAVDRAWER_ITEM_ASSIGNMENTS:
                intent = new Intent(this, AssignmentsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case NAVDRAWER_ITEM_EXAMS:
                intent = new Intent(this, ExamsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case NAVDRAWER_ITEM_SUBJECTS:
                intent = new Intent(this, SubjectsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case NAVDRAWER_ITEM_MANAGE_TIMETABLES:
                intent = new Intent(this, TimetablesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
