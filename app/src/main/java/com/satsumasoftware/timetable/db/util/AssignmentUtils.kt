package com.satsumasoftware.timetable.db.util

import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.schema.AssignmentsSchema
import com.satsumasoftware.timetable.framework.Assignment
import com.satsumasoftware.timetable.receiver.AlarmReceiver
import com.satsumasoftware.timetable.util.DateUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.*

object AssignmentUtils {

    private const val LOG_TAG = "AssignmentUtils"

    @JvmStatic
    fun getAssignmentsForClass(context: Context, classId: Int): ArrayList<Assignment> {
        val assignments = ArrayList<Assignment>()
        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                AssignmentsSchema.TABLE_NAME,
                null,
                "${AssignmentsSchema.COL_CLASS_ID}=?",
                arrayOf(classId.toString()),
                null, null, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            assignments.add(Assignment.from(cursor))
            cursor.moveToNext()
        }
        cursor.close()
        return assignments
    }

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
