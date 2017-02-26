package com.satsumasoftware.timetable.util

import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.handler.ClassTimeHandler
import com.satsumasoftware.timetable.db.handler.ExamHandler
import com.satsumasoftware.timetable.receiver.AlarmReceiver
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * A utility class containing static helper functions for notification-related actions.
 *
 * @see AlarmReceiver
 */
object NotificationUtils {

    private const val LOG_TAG = "NotificationUtils"

    /**
     * Cancels alarms for all timetables add adds back alarms for the current timetable.
     */
    @JvmStatic
    fun refreshAlarms(context: Context, application: TimetableApplication) {
        Log.i(LOG_TAG, "Refreshing alarms...")

        val alarmReceiver = AlarmReceiver()

        val classTimeUtils = ClassTimeHandler(context)
        val examUtils = ExamHandler(context)

        Log.i(LOG_TAG, "Cancelling ALL alarms")

        classTimeUtils.getAllItems().forEach {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.CLASS, it.id)
        }
        examUtils.getAllItems().forEach {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.EXAM, it.id)
        }

        Log.i(LOG_TAG, "Adding alarms for the current timetable " +
                "(id: ${application.currentTimetable!!.id})")

        classTimeUtils.getItems(application).forEach {
            ClassTimeHandler.addAlarmsForClassTime(context, application, it)
        }
        examUtils.getItems(application).forEach { exam ->
            if (exam.date.isAfter(LocalDate.now()) ||
                    (exam.date.isEqual(LocalDate.now()) && exam.startTime.isAfter(LocalTime.now()))) {
                ExamHandler.addAlarmForExam(context, exam)
            }
        }

        NotificationUtils.setAssignmentAlarmTime(
                context, PrefUtils.getAssignmentNotificationTime(context))
    }

    /**
     * Changes the time that assignment notifications are shown at.
     */
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
