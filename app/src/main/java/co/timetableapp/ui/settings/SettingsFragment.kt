/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.ui.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import co.timetableapp.BuildConfig
import co.timetableapp.R
import co.timetableapp.licenses.LicensesActivity
import co.timetableapp.util.NotificationUtils
import co.timetableapp.util.PrefUtils
import org.threeten.bp.LocalTime

/**
 * The fragment used to display the app settings.
 *
 * @see SettingsActivity
 */
class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        setupDefaultLessonDurationPref()

        setupNotificationSwitches()
        setupAssignmentNotificationPref()
        setupClassNotificationPref()
        setupExamNotificationPref()
        setupEventNotificationPref()

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

        lessonDurationPref.setOnPreferenceChangeListener { _, newValue ->
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

        assignmentNotificationPref.setOnPreferenceClickListener { preference ->
            displayAssignmentTimePicker(preference!!)
            true
        }
    }

    private fun displayAssignmentTimePicker(preference: Preference) {
        val time = PrefUtils.getAssignmentNotificationTime(activity)

        val initialHour = time.hour
        val initialMinute = time.minute

        TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val newTime = LocalTime.of(hour, minute)

            preference.summary = getString(
                    R.string.pref_assignmentNotificationTime_summary,
                    newTime.toString())

            PrefUtils.setAssignmentNotificationTime(activity, newTime)

        }, initialHour, initialMinute, true).show()
    }

    private fun setupNotificationSwitches() {
        val classSwitch = findPreference(PrefUtils.PREF_ENABLE_CLASS_NOTIFICATIONS)
        classSwitch.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == false) {
                NotificationUtils.cancelClassAlarms(activity)
            } else {
                NotificationUtils.addCurrentClassAlarms(activity, activity.application)
            }
            true
        }

        val assignmentSwitch = findPreference(PrefUtils.PREF_ENABLE_ASSIGNMENT_NOTIFICATIONS)
        assignmentSwitch.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == false) {
                NotificationUtils.cancelAssignmentAlarms(activity)
            } else {
                NotificationUtils.setAssignmentAlarms(activity)
            }
            true
        }

        val examSwitch = findPreference(PrefUtils.PREF_ENABLE_EXAM_NOTIFICATIONS)
        examSwitch.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == false) {
                NotificationUtils.cancelExamAlarms(activity)
            } else {
                NotificationUtils.addCurrentExamAlarms(activity, activity.application)
            }
            true
        }

        val eventSwitch = findPreference(PrefUtils.PREF_ENABLE_EVENT_NOTIFICATIONS)
        eventSwitch.setOnPreferenceChangeListener { _, newValue ->
            if (newValue == false) {
                NotificationUtils.cancelEventAlarms(activity)
            } else {
                NotificationUtils.addCurrentEventAlarms(activity, activity.application)
            }
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

        classNotificationPref.setOnPreferenceChangeListener { _, newValue ->
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

        examNotificationPref.setOnPreferenceChangeListener { _, newValue ->
            val minsBefore = newValue as String
            updateSummaryText(minsBefore.toInt())
            NotificationUtils.refreshExamAlarms(activity, activity.application)
            true
        }
    }

    private fun setupEventNotificationPref() {
        val eventNotificationPref = findPreference(PrefUtils.PREF_EVENT_NOTIFICATION_TIME)

        fun updateSummaryText(minsBefore: Int) {
            eventNotificationPref.summary =
                    getString(R.string.pref_eventNotificationTime_summary, minsBefore)
        }

        updateSummaryText(PrefUtils.getEventNotificationTime(activity))

        eventNotificationPref.setOnPreferenceChangeListener { _, newValue ->
            val minsBefore = newValue as String
            updateSummaryText(minsBefore.toInt())
            NotificationUtils.refreshEventAlarms(activity, activity.application)
            true
        }
    }

    private fun setupAboutPrefs() {
        val licensePref = findPreference("pref_about_licenses")
        licensePref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(activity, LicensesActivity::class.java))
            true
        }

        val githubPref = findPreference("pref_view_github")
        githubPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://github.com/FarbodSalamat-Zadeh/TimetableApp"
            intent.data = Uri.parse(url)
            startActivity(intent)
            true
        }

        val versionPref = findPreference("pref_about_app_version")
        versionPref.summary = BuildConfig.VERSION_NAME
    }

}
