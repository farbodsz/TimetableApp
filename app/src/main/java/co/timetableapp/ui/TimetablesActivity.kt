package co.timetableapp.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import co.timetableapp.R
import co.timetableapp.framework.Timetable

/**
 * An activity for displaying a list of timetables to the user.
 *
 * Note that unlike other activities, there cannot be a placeholder background since there is always
 * at least one existing timetable in the app's database.
 *
 * @see Timetable
 * @see TimetablesFragment
 * @see TimetableEditActivity
 */
class TimetablesActivity : NavigationDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_list)

        setupToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, TimetablesFragment())
                    .commit()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavDrawerItem() = NavigationDrawerActivity.NAVDRAWER_ITEM_MANAGE_TIMETABLES

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

}
