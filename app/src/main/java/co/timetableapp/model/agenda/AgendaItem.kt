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
import android.os.Parcelable
import co.timetableapp.model.Assignment
import co.timetableapp.model.Event
import co.timetableapp.model.Exam
import co.timetableapp.model.Subject

/**
 * This should be implemented by classes that can be shown on the 'Agenda' part of the UI.
 */
interface AgendaItem : AgendaListItem, Parcelable {

    /**
     * @return the title for the Agenda item
     */
    fun getDisplayedTitle(): String

    /**
     * @return  the related [Subject] of the Agenda item. If the item does not have a related
     *          subject, then this returns null.
     */
    fun getRelatedSubject(context: Context): Subject?

    /**
     * @return true if the item's date is in the past
     * @see isUpcoming
     */
    fun isInPast(): Boolean

    /**
     * @return true the item's date is in the future (not in the past)
     * @see isInPast
     */
    fun isUpcoming() = !isInPast()

    override fun isHeader() = false

    /**
     * Defines a sorting order for Agenda items, first being sorted in reverse by date (so that when
     * viewing past assignments, the most recent is shown first), then lexicographically.
     */
    class ReverseDateComparator : Comparator<AgendaItem> {

        override fun compare(o1: AgendaItem?, o2: AgendaItem?): Int {
            val dateComparison = o2!!.getDateTime().compareTo(o1!!.getDateTime())
            return if (dateComparison == 0) {
                o1.getDisplayedTitle().compareTo(o2.getDisplayedTitle())
            } else {
                dateComparison
            }
        }
    }

    companion object {

        /**
         * @return an [AgendaType] from the type of the [agendaItem].
         */
        fun agendaTypeFrom(agendaItem: AgendaItem) = when (agendaItem) {
            is Assignment -> AgendaType.ASSIGNMENT
            is Exam -> AgendaType.EXAM
            is Event -> AgendaType.EVENT
            else -> throw IllegalArgumentException("invalid agenda item type: $agendaItem")
        }
    }

}
