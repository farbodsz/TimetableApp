package co.timetableapp.model.agenda

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.StringRes
import co.timetableapp.R
import co.timetableapp.model.agenda.AgendaHeader.HeaderType
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.TemporalAdjusters

/**
 * Class to represent headers on lists of Agenda items.
 *
 * @param type  determines the date of the header. This must be a [HeaderType].
 */
class AgendaHeader(val type: HeaderType) : AgendaListItem {

    /**
     * The string resource for the name of the header. This is what will be displayed in the UI.
     */
    @StringRes val nameResId: Int

    /**
     * The kinds of headers, used for initializing instances of this [AgendaHeader] class.
     * Each kind of header represents a period of time such as "Today", "This Week" or "Later".
     */
    enum class HeaderType {
        OVERDUE,
        TODAY,
        TOMORROW,
        THIS_WEEK,
        NEXT_WEEK,
        THIS_MONTH,
        LATER
    }

    init {
        nameResId = when (type) {
            HeaderType.OVERDUE -> R.string.due_overdue
            HeaderType.TODAY -> R.string.due_today
            HeaderType.TOMORROW -> R.string.due_tomorrow
            HeaderType.THIS_WEEK -> R.string.due_this_week
            HeaderType.NEXT_WEEK -> R.string.due_next_week
            HeaderType.THIS_MONTH -> R.string.due_this_month
            HeaderType.LATER -> R.string.due_later
        }
    }

    constructor(source: Parcel) : this(source.readSerializable() as HeaderType)

    override fun isHeader() = true

    override fun getDateTime(): LocalDateTime {
        val today = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)

        // Note that the date returned here is just a start date. For example, the header "This
        // Week" will start from the day after tomorrow even though it covers all Agenda items until
        // the following week (where the next header will be placed).

        return when (type) {
            HeaderType.OVERDUE -> LocalDateTime.MIN
            HeaderType.TODAY -> today
            HeaderType.TOMORROW -> today.plusDays(1)
            HeaderType.THIS_WEEK -> today.plusDays(2) // covers day after tomorrow until next header
            HeaderType.NEXT_WEEK -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        // TODO "Next Week" can be the same as "Tomorrow" if on Sunday - change its definition
            HeaderType.THIS_MONTH -> today.plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.MONDAY))
            HeaderType.LATER -> today.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
        }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeSerializable(type)
    }

    override fun toString(): String {
        val name = when (type) {
            AgendaHeader.HeaderType.OVERDUE -> "Overdue"
            AgendaHeader.HeaderType.TODAY -> "Today"
            AgendaHeader.HeaderType.TOMORROW -> "Tomorrow"
            AgendaHeader.HeaderType.THIS_WEEK -> "This Week"
            AgendaHeader.HeaderType.NEXT_WEEK -> "Next Week"
            AgendaHeader.HeaderType.THIS_MONTH -> "This Month"
            AgendaHeader.HeaderType.LATER -> "Later"
        }
        return "AgendaHeader(date=${getDateTime()}, name=$name)"
    }

    companion object {

        /**
         * @return a list of all distinct possible kinds of [AgendaHeader] as per [HeaderType]
         */
        @JvmStatic
        fun getAllHeaderTypes(): List<AgendaHeader> {
            val agendaHeaders = ArrayList<AgendaHeader>()
            HeaderType.values().mapTo(agendaHeaders) { AgendaHeader(it) }
            return agendaHeaders
        }

        @JvmField
        val CREATOR: Parcelable.Creator<AgendaHeader> = object : Parcelable.Creator<AgendaHeader> {
            override fun createFromParcel(source: Parcel): AgendaHeader = AgendaHeader(source)
            override fun newArray(size: Int): Array<AgendaHeader?> = arrayOfNulls(size)
        }
    }

}
