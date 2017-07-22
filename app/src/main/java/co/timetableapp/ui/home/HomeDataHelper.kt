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

package co.timetableapp.ui.home

import android.app.Activity
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.data.handler.EventHandler
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.Assignment
import co.timetableapp.model.Event
import co.timetableapp.model.Exam
import org.threeten.bp.LocalDate

/**
 * A helper class for formatting and filtering data to show on the home page.
 */
class HomeDataHelper(private val activity: Activity) {

    private val application = activity.application
    private val today = LocalDate.now()

    /**
     * @return a list of all [assignments][Assignment] due today
     */
    fun getAssignmentsToday() = AssignmentHandler(activity).getItems(application).filter {
        it.occursOnDate(today)
    }

    /**
     * @return a list of overdue [assignments][Assignment]
     */
    fun getOverdueAssignments() =
            AssignmentHandler(activity).getItems(application).filter { it.isOverdue() }

    /**
     * @return  a list of [assignments][Assignment] occurring in the next [numOfDays] days, not
     *          including today
     */
    fun getUpcomingAssignments(numOfDays: Long) =
            AssignmentHandler(activity).getItems(application).filter {
                it.dueDate.isAfter(today) && it.dueDate.isBefore(today.plusDays(numOfDays))
            }

    /**
     * @return a list of [exams][Exam] occurring today
     */
    fun getExamsToday() =
            ExamHandler(activity).getItems(application).filter { it.occursOnDate(today) }

    /**
     * @return a list of [exams][Exam] occurring in the next [numOfDays] days, not including today
     */
    fun getUpcomingExams(numOfDays: Long) = ExamHandler(activity).getItems(application).filter {
        it.date.isAfter(today) && it.date.isBefore(today.plusDays(numOfDays))
    }

    /**
     * @return a list of [events][Event] occurring today
     */
    fun getEventsToday() =
            EventHandler(activity).getItems(application).filter { it.occursOnDate(today) }

    /**
     * @return a list of [events][Event] occurring in the next [numOfDays] days, not including today
     */
    fun getUpcomingEvents(numOfDays: Long) = EventHandler(activity).getItems(application).filter {
        it.startDateTime.toLocalDate().isAfter(today) &&
                it.startDateTime.toLocalDate().isBefore(today.plusDays(numOfDays))
    }

}
