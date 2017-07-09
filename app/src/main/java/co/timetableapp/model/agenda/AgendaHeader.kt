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

/**
 * Represent headers on lists of Agenda items.
 *
 * Headers can either be "upcoming", meaning that their date period is relative and dependent on the
 * current day (e.g. "Tomorrow", "Next Week"); or they can be "past", where their date period is
 * displayed as a particular month (e.g. "January 2014", "May 2012").
 *
 * @see UpcomingAgendaHeader
 * @see PastAgendaHeader
 */
abstract class AgendaHeader : AgendaListItem {

    /**
     * @param context the activity context, used to get a string from a string resource if necessary
     *
     * @return the text displayed on the header
     */
    abstract fun getName(context: Context): String

    /**
     * Compares this header with another header, like [compareTo].
     *
     * This should be used instead of simply comparing the dates of headers from [getDateTime]
     * since often the dates for different headers can be the same; for example in
     * [UpcomingAgendaHeader] headers, "Tomorrow" could also be "Next Week".
     *
     * @return  a negative integer, zero, or a positive integer if the header is less than, equal
     *          to, or greater than the [other] header.
     */
    fun compareHeaders(other: AgendaHeader): Int {
        val datetimeComparison = getDateTime().compareTo(other.getDateTime())
        return if (datetimeComparison != 0) {
            datetimeComparison
        } else {
            getPriority().compareTo(other.getPriority())
        }
    }

    /**
     * @return  an integer denoting the "priority" of a header, used to determine the importance of
     *          headers where two headers have the same date. A higher number denotes a higher
     *          priority level.
     */
    abstract fun getPriority(): Int

    override fun isHeader() = true

}
