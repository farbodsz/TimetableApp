package co.timetableapp.util

import android.app.Application
import android.content.Context
import android.util.Log
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.data.handler.EventHandler
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
     *
     * @see refreshClassAlarms
     * @see refreshExamAlarms
     * @see NotificationUtils.setAssignmentAlarms
     */
    @JvmStatic
    fun refreshAlarms(context: Context, application: TimetableApplication) {
        Log.i(LOG_TAG, "Refreshing alarms...")

        refreshClassAlarms(context, application)
        refreshExamAlarms(context, application)

        NotificationUtils.setAssignmentAlarms(
                context, PrefUtils.getAssignmentNotificationTime(context))
    }

    /**
     * Cancels class time alarms from all timetables and adds back alarms for the current timetable.
     *
     * @see cancelClassAlarms
     * @see addCurrentClassAlarms
     */
    @JvmStatic
    fun refreshClassAlarms(context: Context, application: Application) {
        Log.i(LOG_TAG, "Refreshing class time alarms...")
        cancelClassAlarms(context)
        addCurrentClassAlarms(context, application)
    }

    /**
     * Cancels all class time alarms (from all timetables).
     *
     * @see addCurrentClassAlarms
     */
    @JvmStatic
    fun cancelClassAlarms(context: Context) {
        Log.i(LOG_TAG, "Cancelling all class time alarms")
        ClassTimeHandler(context).getAllItems().forEach {
            AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.CLASS, it.id)
        }
    }

    /**
     * Sets class time alarms (only for those that belong to the current timetable).
     *
     * @see cancelClassAlarms
     */
    @JvmStatic
    fun addCurrentClassAlarms(context: Context, application: Application) {
        Log.i(LOG_TAG, "Adding class time alarms for the current timetable")
        ClassTimeHandler(context).getItems(application).forEach {
            ClassTimeHandler.addAlarmsForClassTime(context, application, it)
        }
    }

    /**
     * Cancels exam alarms from all timetables and adds back alarms for the current timetable.
     *
     * @see cancelExamAlarms
     * @see addCurrentExamAlarms
     */
    @JvmStatic
    fun refreshExamAlarms(context: Context, application: Application) {
        Log.i(LOG_TAG, "Refreshing exam alarms...")

        cancelExamAlarms(context)
        addCurrentExamAlarms(context, application)
    }

    /**
     * Cancels all exam alarms (from all timetables).
     *
     * @see addCurrentExamAlarms
     */
    @JvmStatic
    fun cancelExamAlarms(context: Context) {
        Log.i(LOG_TAG, "Cancelling all exam alarms")
        ExamHandler(context).getAllItems().forEach {
            AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.EXAM, it.id)
        }
    }

    /**
     * Sets exam alarms (only for those that belong to the current timetable).
     *
     * @see cancelExamAlarms
     */
    @JvmStatic
    fun addCurrentExamAlarms(context: Context, application: Application) {
        Log.i(LOG_TAG, "Adding exam alarms for the current timetable")
        ExamHandler(context).getItems(application).forEach { exam ->
            val examInPastToday = exam.date.isEqual(LocalDate.now()) && exam.isInPast()

            if (exam.isUpcoming() || examInPastToday) {
                ExamHandler.addAlarmForExam(context, exam)
            }
        }
    }

    /**
     * Changes the time that upcoming assignment notifications and overdue assignment notifications
     * are shown at.
     *
     * @see cancelAssignmentAlarms
     */
    @JvmOverloads
    @JvmStatic
    fun setAssignmentAlarms(context: Context,
                            time: LocalTime = PrefUtils.getAssignmentNotificationTime(context)) {
        Log.i(LOG_TAG, "Setting the alarm notification time to $time...")

        // Cancel the current time
        cancelAssignmentAlarms(context)

        // Repeat every day
        val repeatInterval: Long = 86400000

        // Remind every day at the specified time
        val reminderStartTime = LocalDateTime.of(LocalDate.now(), time)

        Log.i(LOG_TAG, "Setting new assignment repeating alarms")

        AlarmReceiver().setRepeatingAlarm(
                context,
                AlarmReceiver.Type.ASSIGNMENT,
                DateUtils.asCalendar(reminderStartTime),
                AlarmReceiver.ASSIGNMENTS_NOTIFICATION_ID,
                repeatInterval)

        AlarmReceiver().setRepeatingAlarm(
                context,
                AlarmReceiver.Type.ASSIGNMENT_OVERDUE,
                DateUtils.asCalendar(reminderStartTime),
                AlarmReceiver.ASSIGNMENTS_OVERDUE_NOTIFICATION_ID,
                repeatInterval)
    }

    /**
     * Cancels assignment alarms/notifications (both incomplete and overdue notifications).
     *
     * @see setAssignmentAlarms
     */
    @JvmStatic
    fun cancelAssignmentAlarms(context: Context) {
        Log.i(LOG_TAG, "Cancelling assignment alarms")

        AlarmReceiver().cancelAlarm(
                context,
                AlarmReceiver.Type.ASSIGNMENT,
                AlarmReceiver.ASSIGNMENTS_NOTIFICATION_ID)

        AlarmReceiver().cancelAlarm(
                context,
                AlarmReceiver.Type.ASSIGNMENT_OVERDUE,
                AlarmReceiver.ASSIGNMENTS_OVERDUE_NOTIFICATION_ID)
    }

    /**
     * Cancels event alarms from all timetables and adds back alarms for the current timetable.
     *
     * @see cancelEventAlarms
     * @see addCurrentEventAlarms
     */
    @JvmStatic
    fun refreshEventAlarms(context: Context, application: Application) {
        Log.i(LOG_TAG, "Refreshing event alarms...")

        cancelEventAlarms(context)
        addCurrentEventAlarms(context, application)
    }

    /**
     * Cancels all event alarms (from all timetables).
     *
     * @see addCurrentEventAlarms
     */
    @JvmStatic
    fun cancelEventAlarms(context: Context) {
        Log.i(LOG_TAG, "Cancelling all event alarms")
        EventHandler(context).getAllItems().forEach {
            AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.EVENT, it.id)
        }
    }

    /**
     * Sets event alarms (only for those that belong to the current timetable).
     *
     * @see cancelEventAlarms
     */
    @JvmStatic
    fun addCurrentEventAlarms(context: Context, application: Application) {
        Log.i(LOG_TAG, "Adding event alarms for the current timetable")
        EventHandler(context).getItems(application).forEach { event ->
            val examInPastToday =
                    event.startDateTime.toLocalDate().isEqual(LocalDate.now()) && event.isInPast()

            if (event.isUpcoming() || examInPastToday) {
                EventHandler.addAlarmForEvent(context, event)
            }
        }
    }

}
