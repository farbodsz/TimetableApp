package co.timetableapp.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;

import co.timetableapp.R;
import co.timetableapp.framework.Assignment;

/**
 * An activity for displaying a list of assignments to the user.
 *
 * Assignments can be displayed in two formats: {@link #DISPLAY_TODO} and
 * {@link #DISPLAY_ALL_UPCOMING}. In the former, only incomplete assignments will be displayed; in
 * the latter, only assignments that are due in the future (regardless of completion) and overdue
 * assignments will be shown.
 *
 * @see Assignment
 * @see AssignmentsFragment
 * @see AssignmentDetailActivity
 * @see AssignmentEditActivity
 */
public class AssignmentsActivity extends NavigationDrawerActivity {

    /**
     * The intent extra key for the display mode of the assignments.
     *
     * This should be either {@link #DISPLAY_TODO} or {@link #DISPLAY_ALL_UPCOMING}. If the data
     * passed with this key is null, {@link #DISPLAY_ALL_UPCOMING} will be used by default.
     */
    public static final String EXTRA_MODE = "extra_mode";

    /**
     * Suggests that only incomplete assignments will be shown in the list.
     *
     * It is specified by passing it through an intent extra with the {@link #EXTRA_MODE} key.
     *
     * @see #DISPLAY_ALL_UPCOMING
     */
    public static final int DISPLAY_TODO = 1;

    /**
     * Suggests that only assignments due in the future and overdue assignments will be shown in the
     * list.
     *
     * It is specified by passing it through an intent extra with the {@link #EXTRA_MODE} key.
     *
     * @see #DISPLAY_TODO
     */
    public static final int DISPLAY_ALL_UPCOMING = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_list);

        setupToolbar();

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putInt(AssignmentsFragment.ARGUMENT_MODE, getDisplayMode());

            AssignmentsFragment assignmentsFragment = new AssignmentsFragment();
            assignmentsFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new AssignmentsFragment())
                    .commit();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private int getDisplayMode() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            return extras.getInt(EXTRA_MODE);
        } else {
            return DISPLAY_ALL_UPCOMING;
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_ASSIGNMENTS;
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
    protected NavigationView getSelfNavigationView() {
        return (NavigationView) findViewById(R.id.navigationView);
    }

}
