package com.satsumasoftware.timetable.util

import android.content.Context
import android.preference.PreferenceManager
import com.satsumasoftware.timetable.db.util.AssignmentUtils
import com.satsumasoftware.timetable.framework.Timetable
import org.threeten.bp.LocalTime

class PrefUtils private constructor() {

    companion object {

        const val PREF_CURRENT_TIMETABLE = "pref_current_timetable"

        @JvmStatic
        fun getCurrentTimetable(context: Context): Timetable? {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val timetableId = sp.getInt(PREF_CURRENT_TIMETABLE, -1)
            return if (timetableId == -1) {
                null
            } else {
                Timetable.create(context, timetableId)
            }
        }

        @JvmStatic
        fun setCurrentTimetable(context: Context, timetable: Timetable) {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            sp.edit().putInt(PREF_CURRENT_TIMETABLE, timetable.id).apply()
        }


        const val PREF_DISPLAY_WEEKS_AS_LETTERS = "pref_display_weeks_as_letters"

        @JvmStatic
        fun displayWeeksAsLetters(context: Context): Boolean {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            return sp.getBoolean(PREF_DISPLAY_WEEKS_AS_LETTERS, false)
        }


        const val PREF_DEFAULT_LESSON_DURATION = "pref_default_lesson_duration"

        @JvmStatic
        fun getDefaultLessonDuration(context: Context): Int {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val strDuration = sp.getString(PREF_DEFAULT_LESSON_DURATION, "60")
            return strDuration.toInt()
        }


        const val PREF_ASSIGNMENT_NOTIFICATION_TIME = "pref_assignment_notification_time"

        @JvmStatic
        fun getAssignmentNotificationTime(context: Context): LocalTime {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val strTime = sp.getString(PREF_ASSIGNMENT_NOTIFICATION_TIME, "17:00")
            return LocalTime.parse(strTime)
        }

        @JvmStatic
        fun setAssignmentNotificationTime(context: Context, time: LocalTime) {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            sp.edit().putString(PREF_ASSIGNMENT_NOTIFICATION_TIME, time.toString()).apply()
            AssignmentUtils.setAssignmentAlarmTime(context, time)
        }

    }
}
