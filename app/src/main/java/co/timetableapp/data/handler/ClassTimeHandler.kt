package co.timetableapp.data.handler

import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import co.timetableapp.TimetableApplication
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.ClassTimesSchema
import co.timetableapp.framework.Class
import co.timetableapp.framework.ClassDetail
import co.timetableapp.framework.ClassTime
import co.timetableapp.receiver.AlarmReceiver
import co.timetableapp.util.DateUtils
import co.timetableapp.util.PrefUtils
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.*

class ClassTimeHandler(context: Context) : TimetableItemHandler<ClassTime>(context) {

    override val tableName = ClassTimesSchema.TABLE_NAME

    override val itemIdCol = ClassTimesSchema._ID

    override val timetableIdCol = ClassTimesSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = ClassTime.from(cursor)

    override fun createFromId(id: Int) = ClassTime.create(context, id)

    override fun propertiesAsContentValues(item: ClassTime): ContentValues {
        val values = ContentValues()
        with(values) {
            put(ClassTimesSchema._ID, item.id)
            put(ClassTimesSchema.COL_TIMETABLE_ID, item.timetableId)
            put(ClassTimesSchema.COL_CLASS_DETAIL_ID, item.classDetailId)
            put(ClassTimesSchema.COL_DAY, item.day.value)
            put(ClassTimesSchema.COL_WEEK_NUMBER, item.weekNumber)
            put(ClassTimesSchema.COL_START_TIME_HRS, item.startTime.hour)
            put(ClassTimesSchema.COL_START_TIME_MINS, item.startTime.minute)
            put(ClassTimesSchema.COL_END_TIME_HRS, item.endTime.hour)
            put(ClassTimesSchema.COL_END_TIME_MINS, item.endTime.minute)
        }
        return values
    }

    override fun deleteItem(itemId: Int) {
        super.deleteItem(itemId)

        AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.CLASS, itemId)
    }

    companion object {

        private const val WEEK_AS_MILLISECONDS = 604800000L

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
            val minsBefore = PrefUtils.getClassNotificationTime(context).toLong()
            val startDateTime = LocalDateTime.of(possibleDate,
                    classTime.startTime.minusMinutes(minsBefore)) // remind X mins before start

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

        @JvmStatic
        fun getClassTimesForDetail(context: Context, classDetailId: Int): ArrayList<ClassTime> {
            val classTimesQuery = Query.Builder()
                    .addFilter(Filters.equal(
                            ClassTimesSchema.COL_CLASS_DETAIL_ID, classDetailId.toString()))
                    .build()
            return ClassTimeHandler(context).getAllItems(classTimesQuery)
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

    }

}
