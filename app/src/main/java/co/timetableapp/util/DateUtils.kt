package co.timetableapp.util

import android.app.Application
import android.content.Context
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.WeekFields
import java.util.*

object DateUtils {

    /**
     * [DateTimeFormatter] for showing 24-hour times - e.g. 17:45
     */
    @JvmField val FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm")!!

    /**
     * [DateTimeFormatter] for showing short dates - e.g. 7 May
     */
    @JvmField val FORMATTER_SHORT_DATE = DateTimeFormatter.ofPattern("d MMM")!!

    /**
     * [DateTimeFormatter] for showing dates in full - e.g. "28 September 2014".
     */
    @JvmField val FORMATTER_FULL_DATE = DateTimeFormatter.ofPattern("d MMMM uuuu")!!

    /**
     * [DateTimeFormatter] for showing months with years in full - e.g. "March 2012".
     */
    @JvmField val FORMATTER_MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM uuuu")!!

    /**
     * [DateTimeFormatter] for showing month abbreviations with years - e.g. "Mar 2012".
     */
    @JvmField val FORMATTER_SHORT_MONTH_YEAR = DateTimeFormatter.ofPattern("MMM uuuu")!!

    private const val ID_OVERDUE = 0
    private const val ID_TODAY = 1
    private const val ID_TOMORROW = 2
    private const val ID_THIS_WEEK = 3
    private const val ID_NEXT_WEEK = 4
    private const val ID_THIS_MONTH = 5
    private const val ID_LATER = 6

    @JvmStatic
    fun getDatePeriodId(dueDate: LocalDate): Int {
        val now = LocalDate.now()
        val woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()  // to get week of year
        return when {
            dueDate.isBefore(now) -> ID_OVERDUE
            dueDate.isEqual(now) -> ID_TODAY
            dueDate.isEqual(now.plusDays(1)) -> ID_TOMORROW
            dueDate.year == now.year && dueDate.get(woy) == now.get(woy) -> ID_THIS_WEEK
            dueDate.year == now.year && dueDate.get(woy) == now.plusWeeks(1).get(woy) -> ID_NEXT_WEEK
            dueDate.year == now.year && dueDate.monthValue == now.monthValue -> ID_THIS_MONTH
            else -> ID_LATER
        }
    }

    @JvmStatic
    fun makeHeaderName(context: Context, timePeriodId: Int): String {
        val stringRes = when (timePeriodId) {
            ID_OVERDUE -> R.string.due_overdue
            ID_TODAY -> R.string.due_today
            ID_TOMORROW -> R.string.due_tomorrow
            ID_THIS_WEEK -> R.string.due_this_week
            ID_NEXT_WEEK -> R.string.due_next_week
            ID_THIS_MONTH -> R.string.due_this_month
            ID_LATER -> R.string.due_later
            else -> throw IllegalArgumentException("invalid time period id '$timePeriodId'")
        }
        return context.getString(stringRes)
    }

    @JvmOverloads
    @JvmStatic
    fun findWeekNumber(application: Application, localDate: LocalDate = LocalDate.now()): Int {
        val timetable = (application as TimetableApplication).currentTimetable!!

        val days = Period.between(timetable.startDate, localDate).days
        val nthWeek = days / 7

        val weekNo = (nthWeek % timetable.weekRotations) + 1
        return weekNo
    }

    @JvmStatic
    fun asCalendar(localDateTime: LocalDateTime) = GregorianCalendar(
            localDateTime.year,
            localDateTime.monthValue - 1,
            localDateTime.dayOfMonth,
            localDateTime.hour,
            localDateTime.minute)

}
