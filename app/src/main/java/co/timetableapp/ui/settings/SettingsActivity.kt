package co.timetableapp.ui.settings

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import co.timetableapp.R
import co.timetableapp.ui.base.NavigationDrawerActivity

/**
 * An activity to display a list of settings (preferences) that the user can modify.
 *
 * @see SettingsActivity
 */
class SettingsActivity : NavigationDrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fragmentManager.beginTransaction()
                .replace(R.id.content, SettingsFragment())
                .commit()
    }

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_SETTINGS

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

}
