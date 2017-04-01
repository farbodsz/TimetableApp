package co.timetableapp.ui.assignments

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar

import co.timetableapp.R
import co.timetableapp.model.Assignment
import co.timetableapp.ui.base.NavigationDrawerActivity

/**
 * An activity for displaying a list of assignments to the user.
 *
 * @see Assignment
 * @see AssignmentsFragment
 * @see AssignmentDetailActivity
 * @see AssignmentEditActivity
 */
class AssignmentsActivity : NavigationDrawerActivity() {

    companion object {

        /**
         * The intent extra key for the display mode of the assignments.
         *
         * This should be either [DISPLAY_TODO] or [DISPLAY_ALL_UPCOMING]. If the data passed with
         * this key is null, [DISPLAY_ALL_UPCOMING] will be used by default.
         */
        const val EXTRA_MODE = "extra_mode"

        /**
         * Suggests that only incomplete assignments will be shown in the list.
         *
         * It is specified by passing it through an intent extra with the [EXTRA_MODE] key.
         *
         * @see DISPLAY_ALL_UPCOMING
         */
        const val DISPLAY_TODO = 1

        /**
         * Suggests that only assignments due in the future and overdue assignments will be shown in
         * the list.
         *
         * It is specified by passing it through an intent extra with the [.EXTRA_MODE] key.
         *
         * @see DISPLAY_TODO
         */
        const val DISPLAY_ALL_UPCOMING = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_list)

        setupToolbar()

        if (savedInstanceState == null) {
            val args = Bundle()
            args.putInt(AssignmentsFragment.ARGUMENT_MODE, displayMode)

            val fragment = AssignmentsFragment()
            fragment.arguments = args

            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, AssignmentsFragment())
                    .commit()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    private val displayMode: Int
        get() {
            val extras = intent.extras

            if (extras != null) {
                return extras.getInt(EXTRA_MODE)
            } else {
                return DISPLAY_ALL_UPCOMING
            }
        }

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_ASSIGNMENTS

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

}
