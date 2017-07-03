package co.timetableapp.model.agenda

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.util.DateUtils
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.YearMonth

/**
 * Class to represent headers for past dates on lists of Agenda items.
 *
 * This kind of [AgendaHeader] would be shown in the UI as combinations of a year and a month, such
 * as "May 2015" or "November 2011".
 *
 * These headers should be sorted using [AgendaListItem.ReverseComparator] which is why
 * [getDateTime] returns the last possible datetime in the [yearMonth]
 *
 * @param yearMonth determines the date of the header
 *
 * @see UpcomingAgendaHeader
 */
data class PastAgendaHeader(val yearMonth: YearMonth) : AgendaHeader() {

    constructor(source: Parcel) : this(source.readSerializable() as YearMonth)

    override fun getName(context: Context) = getDateTime().format(DateUtils.FORMATTER_MONTH_YEAR)!!

    override fun getPriority() = 0 // all custom headers have the same priority

    override fun getDateTime() =
            LocalDateTime.of(yearMonth.atDay(yearMonth.lengthOfMonth()), LocalTime.MAX)!!

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeSerializable(yearMonth)
    }

    companion object {

        /**
         * Constructs a [PastAgendaHeader] based on any [dateTime]
         */
        @JvmStatic
        fun from(dateTime: LocalDateTime) = PastAgendaHeader(YearMonth.from(dateTime))

        @JvmField
        val CREATOR: Parcelable.Creator<PastAgendaHeader> = object : Parcelable.Creator<PastAgendaHeader> {
            override fun createFromParcel(source: Parcel): PastAgendaHeader = PastAgendaHeader(source)
            override fun newArray(size: Int): Array<PastAgendaHeader?> = arrayOfNulls(size)
        }
    }

}
