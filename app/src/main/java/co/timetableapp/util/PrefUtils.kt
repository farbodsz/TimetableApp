package co.timetableapp.util

import android.content.Context
import android.preference.PreferenceManager
import co.timetableapp.model.Timetable
import org.threeten.bp.LocalTime

object PrefUtils {

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


    private const val PREF_SHOW_WEEK_ROTATIONS_WITH_NUMBERS = "pref_show_week_rotations_with_numbers"

    @JvmStatic
    fun isWeekRotationShownWithNumbers(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean(PREF_SHOW_WEEK_ROTATIONS_WITH_NUMBERS, false)
    }

    @JvmStatic
    fun setWeekRotationShownWithNumbers(context: Context, displayWithNumbers: Boolean) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        sp.edit().putBoolean(PREF_SHOW_WEEK_ROTATIONS_WITH_NUMBERS, displayWithNumbers).apply()
    }


    const val PREF_DEFAULT_LESSON_DURATION = "pref_default_lesson_duration"

    @JvmStatic
    fun getDefaultLessonDuration(context: Context): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val strDuration = sp.getString(PREF_DEFAULT_LESSON_DURATION, "60")
        return strDuration.toInt()
    }


    const val PREF_ENABLE_ASSIGNMENT_NOTIFICATIONS = "pref_enable_assignment_notifications"

    @JvmStatic
    fun getAssignmentNotificationsEnabled(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean(PREF_ENABLE_ASSIGNMENT_NOTIFICATIONS, true)
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
        NotificationUtils.setAssignmentAlarms(context, time)
    }


    const val PREF_ENABLE_CLASS_NOTIFICATIONS = "pref_enable_class_notifications"

    @JvmStatic
    fun getClassNotificationsEnabled(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean(PREF_ENABLE_CLASS_NOTIFICATIONS, true)
    }

    const val PREF_CLASS_NOTIFICATION_TIME = "pref_class_notification_time"

    @JvmStatic
    fun getClassNotificationTime(context: Context): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getString(PREF_CLASS_NOTIFICATION_TIME, "5").toInt()
    }


    const val PREF_ENABLE_EXAM_NOTIFICATIONS = "pref_enable_exam_notifications"

    @JvmStatic
    fun getExamNotificationsEnabled(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean(PREF_ENABLE_EXAM_NOTIFICATIONS, true)
    }

    const val PREF_EXAM_NOTIFICATION_TIME = "pref_exam_notification_time"

    @JvmStatic
    fun getExamNotificationTime(context: Context): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getString(PREF_EXAM_NOTIFICATION_TIME, "30").toInt()
    }


    const val PREF_ENABLE_EVENT_NOTIFICATIONS = "pref_enable_event_notifications"

    @JvmStatic
    fun getEventNotificationsEnabled(context: Context): Boolean {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getBoolean(PREF_ENABLE_EVENT_NOTIFICATIONS, true)
    }

    const val PREF_EVENT_NOTIFICATION_TIME = "pref_event_notification_time"

    @JvmStatic
    fun getEventNotificationTime(context: Context): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return sp.getString(PREF_EVENT_NOTIFICATION_TIME, "30").toInt()
    }

}
