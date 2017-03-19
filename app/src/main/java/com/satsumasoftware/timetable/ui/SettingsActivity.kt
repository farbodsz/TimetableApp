package com.satsumasoftware.timetable.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import com.satsumasoftware.timetable.BuildConfig
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.licenses.LicensesActivity
import com.satsumasoftware.timetable.util.NotificationUtils
import com.satsumasoftware.timetable.util.PrefUtils
import org.threeten.bp.LocalTime

/**
 * An activity to display a list of settings (preferences) that the user can modify.
 */
class SettingsActivity : NavigationDrawerActivity() {

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
            setupClassNotificationPref()
            setupExamNotificationPref()

            setupAboutPrefs()
        }

        private fun setupDefaultLessonDurationPref() {
            val lessonDurationPref = findPreference(PrefUtils.PREF_DEFAULT_LESSON_DURATION)

            fun updateSummary(defaultDuration: Int) {
                lessonDurationPref.summary = resources.getQuantityString(
                        R.plurals.pref_defaultLessonDuration_summary,
                        defaultDuration,
                        defaultDuration)
            }

            updateSummary(PrefUtils.getDefaultLessonDuration(activity))

            lessonDurationPref.setOnPreferenceChangeListener { preference, newValue ->
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

            assignmentNotificationPref.setOnPreferenceClickListener { preference ->
                displayAssignmentTimePicker(preference!!)
                true
            }
        }

        private fun setupClassNotificationPref() {
            val classNotificationPref = findPreference(PrefUtils.PREF_CLASS_NOTIFICATION_TIME)

            fun updateSummaryText(minsBefore: Int) {
                classNotificationPref.summary =
                        getString(R.string.pref_classNotificationTime_summary, minsBefore)
            }

            updateSummaryText(PrefUtils.getClassNotificationTime(activity))

            classNotificationPref.setOnPreferenceChangeListener { preference, newValue ->
                val minsBefore = newValue as String
                updateSummaryText(minsBefore.toInt())
                NotificationUtils.refreshClassAlarms(activity, activity.application)
                true
            }
        }

        private fun setupExamNotificationPref() {
            val examNotificationPref = findPreference(PrefUtils.PREF_EXAM_NOTIFICATION_TIME)

            fun updateSummaryText(minsBefore: Int) {
                examNotificationPref.summary =
                        getString(R.string.pref_examNotificationTime_summary, minsBefore)
            }

            updateSummaryText(PrefUtils.getExamNotificationTime(activity))

            examNotificationPref.setOnPreferenceChangeListener { preference, newValue ->
                val minsBefore = newValue as String
                updateSummaryText(minsBefore.toInt())
                NotificationUtils.refreshExamAlarms(activity, activity.application)
                true
            }
        }

        private fun setupAboutPrefs() {
            val licensePref = findPreference("pref_about_licenses")
            licensePref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, LicensesActivity::class.java))
                true
            }

            val versionPref = findPreference("pref_about_app_version")
            versionPref.summary = BuildConfig.VERSION_NAME
        }

    }

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavDrawerItem() = NavigationDrawerActivity.NAVDRAWER_ITEM_SETTINGS

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

}
