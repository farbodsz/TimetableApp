package co.timetableapp.model.agenda

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.util.DateUtils
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.YearMonth

/**
 * Class to represent headers for custom dates on lists of Agenda items.
 *
 * This kind of [AgendaHeader] would be shown in the UI as combinations of a year and a month, such
 * as "May 2015" or "November 2011".
 *
 * @param yearMonth determines the date of the header
 *
 * @see RelativeAgendaHeader
 */
data class CustomAgendaHeader(val yearMonth: YearMonth) : AgendaHeader() {

    constructor(source: Parcel) : this(source.readSerializable() as YearMonth)

    override fun getName(context: Context) = getDateTime().format(DateUtils.FORMATTER_MONTH_YEAR)!!

    override fun getPriority() = 0 // all custom headers have the same priority

    override fun getDateTime() = LocalDateTime.of(yearMonth.atDay(1), LocalTime.MIDNIGHT)!!

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeSerializable(yearMonth)
    }

    companion object {

        /**
         * Constructs a [CustomAgendaHeader] based on any [dateTime]
         */
        @JvmStatic
        fun from(dateTime: LocalDateTime) = CustomAgendaHeader(YearMonth.from(dateTime))

        @JvmField
        val CREATOR: Parcelable.Creator<CustomAgendaHeader> = object : Parcelable.Creator<CustomAgendaHeader> {
            override fun createFromParcel(source: Parcel): CustomAgendaHeader = CustomAgendaHeader(source)
            override fun newArray(size: Int): Array<CustomAgendaHeader?> = arrayOfNulls(size)
        }
    }

}
