/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.model.agenda

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.StringRes
import co.timetableapp.R
import co.timetableapp.model.agenda.UpcomingAgendaHeader.HeaderType
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.TemporalAdjusters

/**
 * Class to represent headers for upcoming dates on lists of Agenda items.
 *
 * These headers mark a period of type relative to the current day, such as "Today", "Next Week",
 * or even "Overdue".
 *
 * Note: these headers should be sorted using the natural sorting order of [AgendaListItem]
 *
 * @param type  determines the relative period of the header. This must be a [HeaderType].
 *
 * @see PastAgendaHeader
 */
data class UpcomingAgendaHeader(val type: HeaderType) : AgendaHeader() {

    /**
     * The string resource for the name of the header. This is what will be displayed in the UI.
     */
    @StringRes val nameResId: Int

    /**
     * The kinds of headers, used for initializing instances of this class. Each kind of header
     * represents a relative period of time such as "Today", "This Week" or "Later".
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

    override fun getName(context: Context) = context.getString(nameResId)!!

    override fun getPriority() = when (type) {
        HeaderType.OVERDUE -> 7
        HeaderType.TODAY -> 6
        HeaderType.TOMORROW -> 5
        HeaderType.THIS_WEEK -> 4
        HeaderType.NEXT_WEEK -> 3
        HeaderType.THIS_MONTH -> 2
        HeaderType.LATER -> 1
    }

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
            HeaderType.OVERDUE -> "Overdue"
            HeaderType.TODAY -> "Today"
            HeaderType.TOMORROW -> "Tomorrow"
            HeaderType.THIS_WEEK -> "This Week"
            HeaderType.NEXT_WEEK -> "Next Week"
            HeaderType.THIS_MONTH -> "This Month"
            HeaderType.LATER -> "Later"
        }
        return "UpcomingAgendaHeader(date=${getDateTime()}, name=$name)"
    }

    companion object {

        /**
         * Constructs a [UpcomingAgendaHeader] based on any [dateTime]
         */
        @JvmStatic
        fun from(dateTime: LocalDateTime): UpcomingAgendaHeader {
            // Start from "Later" and iterate until we find where the dateTime is not before the
            // header's dateTime, then return that header.
            getAllHeaderTypes().asReversed()
                    .filterNot { dateTime.isBefore(it.getDateTime()) }
                    .forEach { return it }

            throw NoSuchElementException("could not find a header for the specified dateTime")
        }

        /**
         * @return a list of all distinct possible kinds of [AgendaHeader] as per [HeaderType]
         */
        @JvmStatic
        fun getAllHeaderTypes(): List<UpcomingAgendaHeader> {
            val agendaHeaders = ArrayList<UpcomingAgendaHeader>()
            HeaderType.values().mapTo(agendaHeaders) { UpcomingAgendaHeader(it) }
            return agendaHeaders
        }

        @JvmField
        val CREATOR: Parcelable.Creator<UpcomingAgendaHeader> = object : Parcelable.Creator<UpcomingAgendaHeader> {
            override fun createFromParcel(source: Parcel): UpcomingAgendaHeader = UpcomingAgendaHeader(source)
            override fun newArray(size: Int): Array<UpcomingAgendaHeader?> = arrayOfNulls(size)
        }
    }

}
