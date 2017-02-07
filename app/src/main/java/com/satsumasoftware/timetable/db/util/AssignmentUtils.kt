package com.satsumasoftware.timetable.db.util

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
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
    fun getAssignments(context: Context, application: Application): ArrayList<Assignment> {
        val assignments = ArrayList<Assignment>()

        val timetable = (application as TimetableApplication).currentTimetable!!

        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                AssignmentsSchema.TABLE_NAME,
                null,
                "${AssignmentsSchema.COL_TIMETABLE_ID}=?",
                arrayOf(timetable.id.toString()),
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
    fun getAllAssignments(context: Context): ArrayList<Assignment> {
        val assignments = ArrayList<Assignment>()
        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                AssignmentsSchema.TABLE_NAME, null, null, null, null, null, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            assignments.add(Assignment.from(cursor))
            cursor.moveToNext()
        }
        cursor.close()
        return assignments
    }

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
    fun addAssignment(context: Context, assignment: Assignment) {
        val values = ContentValues()
        with(values) {
            put(AssignmentsSchema._ID, assignment.id)
            put(AssignmentsSchema.COL_TIMETABLE_ID, assignment.timetableId)
            put(AssignmentsSchema.COL_CLASS_ID, assignment.classId)
            put(AssignmentsSchema.COL_TITLE, assignment.title)
            put(AssignmentsSchema.COL_DETAIL, assignment.detail)
            put(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH, assignment.dueDate.dayOfMonth)
            put(AssignmentsSchema.COL_DUE_DATE_MONTH, assignment.dueDate.monthValue)
            put(AssignmentsSchema.COL_DUE_DATE_YEAR, assignment.dueDate.year)
            put(AssignmentsSchema.COL_COMPLETION_PROGRESS, assignment.completionProgress)
        }

        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.insert(AssignmentsSchema.TABLE_NAME, null, values)

        Log.i(LOG_TAG, "Added Assignment with id ${assignment.id}")
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

    @JvmStatic
    fun deleteAssignment(context: Context, assignmentId: Int) {
        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.delete(AssignmentsSchema.TABLE_NAME,
                "${AssignmentsSchema._ID}=?",
                arrayOf(assignmentId.toString()))

        Log.i(LOG_TAG, "Deleted Assignment with id $assignmentId")
    }

    @JvmStatic
    fun replaceAssignment(context: Context, oldAssignmentId: Int, newAssignment: Assignment) {
        Log.i(LOG_TAG, "Replacing Assignment...")
        deleteAssignment(context, oldAssignmentId)
        addAssignment(context, newAssignment)
    }

    @JvmStatic
    fun getHighestAssignmentId(context: Context): Int {
        val db = TimetableDbHelper.getInstance(context).readableDatabase
        val cursor = db.query(
                AssignmentsSchema.TABLE_NAME,
                arrayOf(AssignmentsSchema._ID),
                null,
                null,
                null,
                null,
                "${AssignmentsSchema._ID} DESC")
        if (cursor.count == 0) {
            return 0
        }
        cursor.moveToFirst()
        val highestId = cursor.getInt(cursor.getColumnIndex(AssignmentsSchema._ID))
        cursor.close()
        return highestId
    }

}
