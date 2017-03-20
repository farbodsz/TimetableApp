package co.timetableapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import co.timetableapp.R;
import co.timetableapp.data.PortingFragment;
import co.timetableapp.data.handler.TimetableHandler;
import co.timetableapp.framework.Timetable;
import co.timetableapp.ui.adapter.TimetablesAdapter;
import co.timetableapp.util.UiUtils;

/**
 * An activity for displaying a list of timetables to the user.
 *
 * Note that unlike other activities, there cannot be a placeholder background since there is always
 * at least one existing timetable in the app's database.
 *
 * Clicking on a timetable to view or edit, or choosing to create a new class will direct the user
 * to {@link TimetableEditActivity}.
 *
 * @see Timetable
 * @see TimetableEditActivity
 */
public class TimetablesActivity extends NavigationDrawerActivity {

    private static final int REQUEST_CODE_TIMETABLE_EDIT = 1;

    private ArrayList<Timetable> mTimetables;
    private TimetablesAdapter mAdapter;

    private TimetableHandler mTimetableUtils = new TimetableHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupLayout();
    }

    private void setupLayout() {
        setupList();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimetablesActivity.this, TimetableEditActivity.class);
                startActivityForResult(intent, REQUEST_CODE_TIMETABLE_EDIT);
            }
        });
    }

    private void setupList() {
        mTimetables = mTimetableUtils.getAllItems();
        sortList();

        mAdapter = new TimetablesAdapter(this, mTimetables, findViewById(R.id.coordinatorLayout));
        mAdapter.setOnEntryClickListener(new TimetablesAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(View view, int position) {
                Intent intent = new Intent(TimetablesActivity.this, TimetableEditActivity.class);
                intent.putExtra(TimetableEditActivity.EXTRA_TIMETABLE, mTimetables.get(position));

                Bundle bundle = null;
                if (UiUtils.isApi21()) {
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    TimetablesActivity.this,
                                    view,
                                    getString(R.string.transition_1));
                    bundle = options.toBundle();
                }

                ActivityCompat.startActivityForResult(
                        TimetablesActivity.this, intent, REQUEST_CODE_TIMETABLE_EDIT, bundle);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
    }

    private void sortList() {
        Collections.sort(mTimetables, new Comparator<Timetable>() {
            @Override
            public int compare(Timetable t1, Timetable t2) {
                return t1.getStartDate().compareTo(t2.getStartDate());
            }
        });
    }

    private void refreshList() {
        mTimetables.clear();
        mTimetables.addAll(mTimetableUtils.getAllItems());
        sortList();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TIMETABLE_EDIT) {
            if (resultCode == RESULT_OK) {
                refreshList();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timetables, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles import/export options.
        int portType = -1;
        switch (item.getItemId()) {
            case R.id.action_export:
                portType = PortingFragment.TYPE_EXPORT;
                break;
            case R.id.action_import:
                portType = PortingFragment.TYPE_IMPORT;
                break;
        }
       startPortingFragment(portType);

        return super.onOptionsItemSelected(item);
    }

    private void startPortingFragment(int portType) {
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putInt(PortingFragment.ARGUMENT_PORT_TYPE, portType);

        Fragment portFragment = new PortingFragment();
        portFragment.setArguments(fragmentArgs);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(portFragment, null);
        transaction.commit();
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
        return NAVDRAWER_ITEM_MANAGE_TIMETABLES;
    }

    @Override
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
