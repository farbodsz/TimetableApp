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
import co.timetableapp.R
import co.timetableapp.model.Assignment
import co.timetableapp.model.Color
import co.timetableapp.model.Event
import co.timetableapp.model.home.HomeItem
import co.timetableapp.model.home.HomeItemProperties

/**
 * A home item displaying the number of overdue assignments.
 *
 * @param overdueAssignments    a list of overdue assignments
 */
class OverdueAssignmentsItem(private val overdueAssignments: List<Assignment>) : HomeItem {

    private val numOverdue = overdueAssignments.size

    override fun getHomeItemProperties(activity: Activity) = object : HomeItemProperties {

        override val title: String
            get() = activity.resources.getQuantityString(
                    R.plurals.assignments_overdue_text,
                    numOverdue,
                    numOverdue
            )

        override val subtitle: String?
            get() {
                return if (numOverdue == 1) {
                    overdueAssignments[0].title
                } else {
                    // Add the first 4 assignments or less (if there aren't 4 assignments)
                    // Note that in the UI, only up to one line will be displayed.
                    val stringBuilder = StringBuilder()
                    (0..numOverdue - 1)
                            .takeWhile { it < 4 }
                            .forEach {
                                stringBuilder.append(overdueAssignments[it].title).append(", ")
                            }
                    stringBuilder.removeSuffix(", ").toString()
                }
            }

        override val time: String = ""

        override val extraText: String? = null

        override val color: Color = Event.DEFAULT_COLOR // TODO change to use color resources
    }

}
