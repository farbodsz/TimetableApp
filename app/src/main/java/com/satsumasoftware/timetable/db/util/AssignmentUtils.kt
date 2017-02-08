package com.satsumasoftware.timetable.db.util

import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.receiver.AlarmReceiver
import com.satsumasoftware.timetable.util.DateUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

object AssignmentUtils {

    private const val LOG_TAG = "AssignmentUtils"

    @JvmStatic
    fun setAssignmentAlarmTime(context: Context, time: LocalTime) {
        Log.d(LOG_TAG, "Setting the alarm notification time to $time")

        // Cancel the current time
        AlarmReceiver().cancelAlarm(
                context,
                AlarmReceiver.Type.ASSIGNMENT,
                AlarmReceiver.ASSIGNMENTS_NOTIFICATION_ID)

        // Repeat every day
        val repeatInterval: Long = 86400000

        // Remind every day at the specified time
        val reminderStartTime = LocalDateTime.of(LocalDate.now(), time)
        AlarmReceiver().setRepeatingAlarm(context,
                AlarmReceiver.Type.ASSIGNMENT,
                DateUtils.asCalendar(reminderStartTime),
                AlarmReceiver.ASSIGNMENTS_NOTIFICATION_ID,
                repeatInterval)
    }

}
