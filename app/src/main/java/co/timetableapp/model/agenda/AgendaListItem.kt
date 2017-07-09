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

import android.os.Parcelable
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Represents any item which can be displayed on a list of Agenda items, including headers.
 */
interface AgendaListItem : Comparable<AgendaListItem>, Parcelable {

    fun isHeader(): Boolean

    /**
     * @return  the date and time of the item. If there is no time, then [LocalTime.MIDNIGHT] should
     *          be used as the time. This is used for sorting Agenda list items.
     */
    fun getDateTime(): LocalDateTime

    override fun compareTo(other: AgendaListItem): Int{
        val dateTimeComparison = getDateTime().compareTo(other.getDateTime())
        if (dateTimeComparison != 0) {
            return dateTimeComparison
        }

        // If same date, headers should go first
        val itemTypeComparison = compareItemTypes(other)
        if (itemTypeComparison != 0) {
            return itemTypeComparison
        }

        // Item type comparison is 0 so both must be headers
        return (this as AgendaHeader).compareHeaders(other as AgendaHeader)
    }

    /**
     * @return  an integer between -1 and 1 inclusive based on the comparing whether this item and
     *          the [other] item are headers or not.
     */
    private fun compareItemTypes(other: AgendaListItem): Int {
        return if (isHeader()) {
            if (other.isHeader()) {
                0 // equal - both are headers
            } else {
                -1 // this header comes before the other item
            }
        } else {
            1
        }
    }

    /**
     * Defines a sorting order for agenda list items, being sorted by date then lexicographically.
     * Headers will always be shown before the group of items for that date.
     *
     * Avoid simply reversing a list sorted by [AgendaListItem.compareTo] since it will not take
     * into account that headers should be shown before other item types.
     *
     * Note: this may not work where the headers are [UpcomingAgendaHeader]s.
     */
    class ReverseComparator : Comparator<AgendaListItem> {

        override fun compare(o1: AgendaListItem, o2: AgendaListItem): Int {
            val dateTimeComparison = o2.getDateTime().compareTo(o1.getDateTime())
            if (dateTimeComparison != 0) {
                return dateTimeComparison
            }

            // Order of item types doesn't change - headers still go before agenda items
            val itemTypeComparison = o1.compareItemTypes(o2)
            if (itemTypeComparison != 0) {
                return itemTypeComparison
            }

            // Item type comparison is 0 so both must be headers
            return (o2 as AgendaHeader).compareHeaders(o1 as AgendaHeader)
        }
    }

}
