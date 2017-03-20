package co.timetableapp.util

import android.app.Application
import android.content.Context
import android.util.Log
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.receiver.AlarmReceiver
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
     * Cancels all alarms for all timetables add adds back alarms for the current timetable.
     */
    @JvmStatic
    fun refreshAlarms(context: Context, application: TimetableApplication) {
        Log.i(LOG_TAG, "Refreshing alarms...")

        refreshClassAlarms(context, application)
        refreshExamAlarms(context, application)

        NotificationUtils.setAssignmentAlarmTime(
                context, PrefUtils.getAssignmentNotificationTime(context))
    }

    @JvmStatic
    fun refreshClassAlarms(context: Context, application: Application) {
        Log.i(LOG_TAG, "Refreshing class time alarms...")

        val classTimeUtils = ClassTimeHandler(context)

        Log.v(LOG_TAG, "Cancelling all class time alarms")
        classTimeUtils.getAllItems().forEach {
            AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.CLASS, it.id)
        }

        Log.v(LOG_TAG, "Adding class time alarms for the current timetable")
        classTimeUtils.getItems(application).forEach {
            ClassTimeHandler.addAlarmsForClassTime(context, application, it)
        }
    }

    @JvmStatic
    fun refreshExamAlarms(context: Context, application: Application) {
        Log.i(LOG_TAG, "Refreshing exam alarms...")

        val examUtils = ExamHandler(context)

        Log.v(LOG_TAG, "Cancelling all exam alarms")
        examUtils.getAllItems().forEach {
            AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.EXAM, it.id)
        }

        Log.v(LOG_TAG, "Adding exam alarms for the current timetable")
        examUtils.getItems(application).forEach { exam ->
            val examInPastToday =
                    exam.date.isEqual(LocalDate.now()) && exam.startTime.isAfter(LocalTime.now())

            if (exam.date.isAfter(LocalDate.now()) || examInPastToday) {
                ExamHandler.addAlarmForExam(context, exam)
            }
        }
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
