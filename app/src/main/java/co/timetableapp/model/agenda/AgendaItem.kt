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
import android.support.annotation.StringRes
import co.timetableapp.model.*
import org.threeten.bp.LocalDate

/**
 * This should be implemented by classes that can be shown on the 'Agenda' part of the UI.
 */
interface AgendaItem : AgendaListItem, TimetableItem, Parcelable {

    /**
     * @return the string resource for the name of the kind of [AgendaItem].
     */
    @StringRes
    fun getTypeNameRes(): Int

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
     *
     * @see isUpcoming
     * @see occursOnDate
     */
    fun isInPast(): Boolean

    /**
     * @return true if the item's date is in the future (not in the past)
     *
     * @see isInPast
     * @see occursOnDate
     */
    fun isUpcoming() = !isInPast()

    /**
     * @return true if the item occurs on the specified [date]
     */
    fun occursOnDate(date: LocalDate): Boolean

    override fun isHeader() = false

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
