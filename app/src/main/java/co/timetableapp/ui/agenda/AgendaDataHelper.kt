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

package co.timetableapp.ui.agenda

import android.app.Activity
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.data.handler.EventHandler
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.Assignment
import co.timetableapp.model.agenda.*

/**
 * A helper class for formatting and filtering data to show on the agenda pages.
 */
class AgendaDataHelper(val activity: Activity, val filterParams: AgendaFilterParams) {

    /**
     * Populates the list with items from the database and sorts them with headers added.
     */
    fun createList(items: ArrayList<AgendaListItem>) {
        items.clear()
        items.addAll(fetchAgendaListItems())

        sortWithHeaders(items)
    }

    /**
     * @return an unsorted list of all agenda items (excluding headers)
     *
     * @see getAssignments
     * @see getExams
     * @see getEvents
     */
    private fun fetchAgendaListItems(): List<AgendaListItem> {
        val arrayList = ArrayList<AgendaListItem>()

        val showCompleted = filterParams.showCompleted
        val showPast = filterParams.showPast

        val typesShowing = filterParams.typesToShow

        if (typesShowing.contains(AgendaType.ASSIGNMENT)) {
            arrayList.addAll(getAssignments(showPast, showCompleted))
        }
        if (typesShowing.contains(AgendaType.EXAM)) arrayList.addAll(getExams(showPast))
        if (typesShowing.contains(AgendaType.EVENT)) arrayList.addAll(getEvents(showPast))

        return arrayList
    }

    /**
     * @return a list of assignments filtered depending on the boolean parameters.
     */
    private fun getAssignments(showPast: Boolean, showCompleted: Boolean): List<Assignment> {
        return AssignmentHandler(activity).getItems(activity.application)
                .filter {
                    if (showPast) {
                        it.isPastAndDone()
                    } else {
                        if (showCompleted) {
                            // If showing completed items, then show them with incomplete items
                            it.isUpcoming() || it.isOverdue()
                        } else {
                            // Incomplete items only
                            !it.isComplete()
                        }
                    }
                }
    }

    /**
     * @return a list of exams filtered depending on the value of [showPast]
     */
    private fun getExams(showPast: Boolean) =
            ExamHandler(activity).getItems(activity.application).filter { it.isInPast() == showPast }

    /**
     * @return a list of events filtered depending on the value of [showPast]
     */
    private fun getEvents(showPast: Boolean) =
            EventHandler(activity).getItems(activity.application).filter { it.isInPast() == showPast }

    /**
     * Sorts a list depending on whether we are showing past items.
     *
     * @param items the list of items to sort
     */
    private fun sortItems(items: ArrayList<AgendaListItem>) {
        if (filterParams.showPast) {
            items.sortWith(AgendaListItem.ReverseComparator())
        } else {
            items.sort()
        }
    }

    /**
     * Sorts a list of agenda list items with the required headers added and redundant ones removed.
     *
     * This function should not be invoked after single items have been updated/removed in the list,
     * so it is recommended this is used when the whole list needs to be refreshed.
     *
     * @param items the list of items to sort and add headers to
     *
     * @return the sorted list with headers where needed
     */
    private fun sortWithHeaders(items: ArrayList<AgendaListItem>) {
        val headersToAdd = ArrayList<AgendaListItem>()
        items.forEach {
            addListHeader(it as AgendaItem, headersToAdd)
        }
        items.addAll(headersToAdd)

        sortItems(items)
    }

    /**
     * Adds an [agendaItem] to the list, appropriately so that a new datetime header is also
     * added if necessary, and sorted.
     *
     * This method does not handle database operations.
     *
     * @param agendaItem    the item being added to the list
     * @param items         the list of items being updated
     *
     * @see addListHeader
     */
    fun addListItem(agendaItem: AgendaItem, items: ArrayList<AgendaListItem>) {
        items.add(agendaItem)
        addListHeader(agendaItem, items)

        sortItems(items)

        // Remove the old header that we no longer need
        removeRedundantHeader(items)
    }

    /**
     * Removes any unnecessary headers in the list being displayed in the UI.
     */
    private fun removeRedundantHeader(items: ArrayList<AgendaListItem>) {
        // Removing items whilst iterating causes a ConcurrentModificationException
        // We will go through the list and store which items need to be removed and do that later
        var headerToRemove: AgendaHeader? = null
        var previousItem: AgendaListItem? = null
        for (item in items) {
            if (previousItem == null) {
                previousItem = item
                continue
            }

            if (previousItem.isHeader() && item.isHeader()) {
                // Two consecutive headers found - remove redundant header
                headerToRemove = previousItem as AgendaHeader
                break
            }

            previousItem = item
        }

        headerToRemove?.let {
            // The extra header has been found - remove it
            items.remove(it)
            return
        }

        // The header must be at the end of the list
        val finalItem = items[items.size - 1]
        if (finalItem.isHeader()) {
            headerToRemove = items[items.size - 1] as AgendaHeader
            items.remove(headerToRemove)
        }
    }

    /**
     * Adds an [AgendaHeader] to the [list], only if it is not already in the list.
     *
     * @param agendaItem    determines the date for the header being added
     * @param list          the list being updated
     *
     * @return true if a header was added, otherwise false
     *
     * @see makeHeader
     * @see addListItem
     */
    fun addListHeader(agendaItem: AgendaItem, list: ArrayList<AgendaListItem>): Boolean {
        val header = makeHeader(agendaItem)

        if (!list.contains(header)) {
            list.add(header)
            return true
        } else {
            return false
        }
    }

    /**
     * @return a header based on the [agendaItem]
     */
    private fun makeHeader(agendaItem: AgendaItem) = if (filterParams.showPast) {
        PastAgendaHeader.from(agendaItem.getDateTime())
    } else {
        UpcomingAgendaHeader.from(agendaItem.getDateTime())
    }

}
