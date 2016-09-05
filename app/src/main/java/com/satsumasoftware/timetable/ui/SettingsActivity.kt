package com.satsumasoftware.timetable.ui

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.util.ThemeUtils

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_frame)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = ThemeUtils.tintDrawable(this, R.drawable.ic_done_black_24dp)
        toolbar.setNavigationOnClickListener { actionDone() }

        fragmentManager.beginTransaction()
                .replace(R.id.content, SettingsFragment())
                .commit()
    }

    override fun onBackPressed() {
        actionDone()
        super.onBackPressed()
    }

    fun actionDone() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    class SettingsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
        }

    }
}
