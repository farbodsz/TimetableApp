package com.satsumasoftware.timetable.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import com.satsumasoftware.timetable.BuildConfig
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.util.PrefUtils
import org.threeten.bp.LocalTime

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

            setupDefaultLessonDurationPref()
            setupAssignmentNotificationPref()

            setupAboutPrefs()
        }

        private fun setupDefaultLessonDurationPref() {
            val lessonDurationPref = findPreference(PrefUtils.PREF_DEFAULT_LESSON_DURATION)

            fun updateSummary(defaultDuration: Int) {
                lessonDurationPref.summary = getString(
                        R.string.pref_defaultLessonDuration_summary,
                        defaultDuration)
            }

            updateSummary(PrefUtils.getDefaultLessonDuration(activity))

            lessonDurationPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                val strNewVal = newValue as String
                updateSummary(strNewVal.toInt())
                true
            }
        }

        private fun setupAssignmentNotificationPref() {
            val assignmentNotificationPref =
                    findPreference(PrefUtils.PREF_ASSIGNMENT_NOTIFICATION_TIME)

            assignmentNotificationPref.summary = getString(
                    R.string.pref_assignmentNotificationTime_summary,
                    PrefUtils.getAssignmentNotificationTime(activity).toString())

            fun displayAssignmentTimePicker(preference: Preference) {
                val time = PrefUtils.getAssignmentNotificationTime(activity)

                val initialHour = time.hour
                val initialMinute = time.minute

                TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                    val newTime = LocalTime.of(hour, minute)

                    preference.summary = getString(
                            R.string.pref_assignmentNotificationTime_summary,
                            newTime.toString())

                    PrefUtils.setAssignmentNotificationTime(activity, newTime)

                }, initialHour, initialMinute, true).show()
            }

            assignmentNotificationPref.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                displayAssignmentTimePicker(preference!!)
                true
            }
        }

        private fun setupAboutPrefs() {
            val versionPref = findPreference("pref_about_app_version")
            versionPref.summary = BuildConfig.VERSION_NAME
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
