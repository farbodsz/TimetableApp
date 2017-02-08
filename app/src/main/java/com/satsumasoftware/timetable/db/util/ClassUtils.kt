package com.satsumasoftware.timetable.db.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.DataHandlers
import com.satsumasoftware.timetable.db.DataUtils
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.db.schema.AssignmentsSchema
import com.satsumasoftware.timetable.db.schema.ClassDetailsSchema
import com.satsumasoftware.timetable.db.schema.ClassTimesSchema
import com.satsumasoftware.timetable.framework.Class
import com.satsumasoftware.timetable.framework.ClassDetail
import com.satsumasoftware.timetable.framework.ClassTime
import com.satsumasoftware.timetable.receiver.AlarmReceiver
import com.satsumasoftware.timetable.util.DateUtils
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.*

object ClassUtils {

    private const val LOG_TAG = "ClassUtils"

    private const val WEEK_AS_MILLISECONDS = 604800000L

    @JvmStatic
    fun getClassDetailsForClass(context: Context, classId: Int): ArrayList<ClassDetail> {
        val query = Query.Builder()
                .addFilter(Filters.equal(ClassDetailsSchema.COL_CLASS_ID, classId.toString()))
                .build()
        return DataUtils.getAllItems(DataHandlers.CLASS_DETAILS, context, query)
    }

    @JvmStatic
    fun getClassTimesForDetail(context: Context, classDetailId: Int): ArrayList<ClassTime> {
        val query = Query.Builder()
                .addFilter(Filters.equal(
                        ClassTimesSchema.COL_CLASS_DETAIL_ID, classDetailId.toString()))
                .build()
        return DataUtils.getAllItems(DataHandlers.CLASS_TIMES, context, query)
    }

    @JvmOverloads
    @JvmStatic
    fun getClassTimesForDay(activity: Activity, dayOfWeek: DayOfWeek, weekNumber: Int,
                            date: LocalDate? = null): ArrayList<ClassTime> {
        val classTimes = ArrayList<ClassTime>()

        val timetable = (activity.application as TimetableApplication).currentTimetable!!

        val dbHelper = TimetableDbHelper.getInstance(activity)
        val cursor = dbHelper.readableDatabase.query(
                ClassTimesSchema.TABLE_NAME,
                null,
                "${ClassTimesSchema.COL_TIMETABLE_ID}=? AND ${ClassTimesSchema.COL_DAY}=? " +
                        "AND ${ClassTimesSchema.COL_WEEK_NUMBER}=?",
                arrayOf(timetable.id.toString(), dayOfWeek.value.toString(),
                        weekNumber.toString()),
                null, null, null)

        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val classTime = ClassTime.from(cursor)
            val classDetail = ClassDetail.create(activity, classTime.classDetailId)
            val cls = Class.create(activity, classDetail.classId)!!

            if (!cls.hasStartEndDates() || date == null) {
                classTimes.add(classTime)

            } else if (!cls.startDate.isAfter(date) && !cls.endDate.isBefore(date)) {
                classTimes.add(classTime)
            }

            cursor.moveToNext()
        }
        cursor.close()

        return classTimes
    }

    @JvmStatic
    fun addAlarmsForClassTime(activity: Activity, classTime: ClassTime) =
            addAlarmsForClassTime(activity, activity.application, classTime)

    @JvmStatic
    fun addAlarmsForClassTime(context: Context, application: Application,
                              classTime: ClassTime) {
        // First, try to find a suitable start date for the alarms

        var possibleDate = if (classTime.day != LocalDate.now().dayOfWeek ||
                classTime.startTime.minusMinutes(5).isBefore(LocalTime.now())) {
            // Class is on a different day of the week OR the 5 minute start notice has passed

            val adjuster = TemporalAdjusters.next(classTime.day)
            LocalDate.now().with(adjuster)

        } else {
            // Class is on the same day of the week (AND it has not yet begun)
            LocalDate.now()
        }

        while (DateUtils.findWeekNumber(application, possibleDate)
                != classTime.weekNumber) {
            // Find a week with the correct week number
            possibleDate = possibleDate.plusWeeks(1)
        }

        // Make a LocalDateTime using the calculated start date and ClassTime
        val startDateTime = LocalDateTime.of(possibleDate,
                classTime.startTime.minusMinutes(5)) // remind 5 mins before start

        // Find the repeat interval in milliseconds (for the alarm to repeat)
        val timetable = (application as TimetableApplication).currentTimetable!!
        val repeatInterval = timetable.weekRotations * WEEK_AS_MILLISECONDS

        // Set repeating alarm
        AlarmReceiver().setRepeatingAlarm(context,
                AlarmReceiver.Type.CLASS,
                DateUtils.asCalendar(startDateTime),
                classTime.id,
                repeatInterval)
    }

    private fun deleteClassTime(context: Context, classTimeId: Int) {
        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.delete(ClassTimesSchema.TABLE_NAME,
                "${ClassTimesSchema._ID}=?",
                arrayOf(classTimeId.toString()))

        AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.CLASS, classTimeId)

        Log.i(LOG_TAG, "Deleted ClassTime with id $classTimeId")
    }

    @JvmStatic
    fun completelyDeleteClass(context: Context, cls: Class) {
        Log.i(LOG_TAG, "Deleting everything related to Class of id ${cls.id}")

        DataUtils.deleteItem(DataHandlers.CLASSES, context, cls.id)

        val assignmentsQuery = Query.Builder()
                .addFilter(Filters.equal(AssignmentsSchema.COL_CLASS_ID, cls.id.toString()))
                .build()

        for (assignment in DataUtils.getAllItems(DataHandlers.ASSIGNMENTS, context, assignmentsQuery)) {
            DataUtils.deleteItem(DataHandlers.ASSIGNMENTS, context, assignment.id)
        }

        for (classDetail in getClassDetailsForClass(context, cls.id)) {
            completelyDeleteClassDetail(context, classDetail.id)
        }
    }

    @JvmStatic
    fun completelyDeleteClassDetail(context: Context, classDetailId: Int) {
        Log.i(LOG_TAG, "Deleting everything related to ClassDetail of id $classDetailId")

        DataUtils.deleteItem(DataHandlers.CLASS_DETAILS, context, classDetailId)

        for (classTime in getClassTimesForDetail(context, classDetailId)) {
            completelyDeleteClassTime(context, classTime.id)
        }
    }

    @JvmStatic
    fun completelyDeleteClassTime(context: Context, classTimeId: Int) {
        Log.i(LOG_TAG, "Deleting everything related to ClassTime of id $classTimeId")

        deleteClassTime(context, classTimeId)
    }

}
