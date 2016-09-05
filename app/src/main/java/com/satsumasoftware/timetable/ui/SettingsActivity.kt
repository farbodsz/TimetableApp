package com.satsumasoftware.timetable.ui

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import com.satsumasoftware.timetable.R

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_frame)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fragmentManager.beginTransaction()
                .replace(R.id.content, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
        }

    }

    override fun getSelfToolbar(): Toolbar {
        return findViewById(R.id.toolbar) as Toolbar
    }

    override fun getSelfDrawerLayout(): DrawerLayout {
        return findViewById(R.id.drawerLayout) as DrawerLayout
    }

    override fun getSelfNavDrawerItem(): Int {
        return BaseActivity.NAVDRAWER_ITEM_SETTINGS
    }

    override fun getSelfNavigationView(): NavigationView {
        return findViewById(R.id.navigationView) as NavigationView
    }

}
