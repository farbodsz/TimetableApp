package co.timetableapp.util

import android.content.Context
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
     * @return a list of [ClassTime]s for a particular day
     */
    // TODO improve parameters
    @JvmStatic
    fun getClassTimesForDay(context: Context,
                            currentTimetable: Timetable,
                            dayOfWeek: DayOfWeek,
                            weekNumber: Int,
                            date: LocalDate): ArrayList<ClassTime> {
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
