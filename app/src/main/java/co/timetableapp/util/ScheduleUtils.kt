package co.timetableapp.util

import android.app.Application
import android.content.Context
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.ClassTimesSchema
import co.timetableapp.model.Class
import co.timetableapp.model.ClassDetail
import co.timetableapp.model.ClassTime
import co.timetableapp.model.Timetable
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * A utility class containing helper methods related to the user's schedule.
 */
object ScheduleUtils {

    /**
     * @return  the [ClassTime]s occurring on a particular [date] for the currently selected
     *          timetable. Note that if the specified [date] is not within the start and end dates
     *          of the current timetable, the returned list will be empty (the timetable would not
     *          have started or would have ended).
     */
    @JvmStatic
    fun getClassTimesForDate(context: Context,
                             application: Application,
                             date: LocalDate): ArrayList<ClassTime> {
        val timetable = (application as TimetableApplication).currentTimetable!!
        if (!timetable.isValidToday(date)) {
            // Return empty list if timetable hasn't started or has ended
            return ArrayList()
        }

        val weekNumber = DateUtils.findWeekNumber(application, date)

        return getClassTimesForDate(context, timetable, date, date.dayOfWeek, weekNumber)
    }

    /**
     * Returns the class times occurring on the specified [date].
     *
     * Note that if the specified [date] is not within the start and end dates of the
     * [currentTimetable], then an empty list will be returned as the timetable would either not
     * have started or would have ended during that date.
     *
     * @param context           the activity context
     * @param currentTimetable  the current timetable as in [TimetableApplication.currentTimetable]
     * @param date              the date to find [ClassTime]s for
     * @param dayOfWeek         the day of the week of the [date] (e.g. Monday, Tuesday, etc.)
     * @param weekNumber        the week number of the [date] according to the scheduling pattern
     *                          set by [Timetable.weekRotations]. For example, '2' if it the date
     *                          occurs on a 'Week 2' or 'Week B'.
     *
     * @return the list of [ClassTime]s which occur on the specified [date]
     */
    @JvmStatic
    fun getClassTimesForDate(context: Context,
                             currentTimetable: Timetable,
                             date: LocalDate,
                             dayOfWeek: DayOfWeek,
                             weekNumber: Int): ArrayList<ClassTime> {
        if (!currentTimetable.isValidToday(date)) {
            return ArrayList()
        }

        val timetableId = currentTimetable.id

        val query = Query.Builder()
                .addFilter(Filters.equal(ClassTimesSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .addFilter(Filters.equal(ClassTimesSchema.COL_DAY, dayOfWeek.value.toString()))
                .addFilter(Filters.equal(ClassTimesSchema.COL_WEEK_NUMBER, weekNumber.toString()))
                .build()

        val classTimes = ArrayList<ClassTime>()

        for (classTime in ClassTimeHandler(context).getAllItems(query)) {
            try {
                val classDetail = ClassDetail.create(context, classTime.classDetailId)
                val cls = Class.create(context, classDetail.classId)

                if (cls.isCurrent(date)) {
                    classTimes.add(classTime)
                }
            } catch (e: DataNotFoundException) {
                // Don't display to the user if the item can't be found but print the stack trace
                e.printStackTrace()
                continue
            }
        }

        return classTimes
    }

}
