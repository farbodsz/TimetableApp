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

package co.timetableapp.ui.classes

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.model.ClassTime
import co.timetableapp.ui.OnItemClickListener
import co.timetableapp.util.title

/**
 * A RecyclerView adapter to display [times of classes][ClassTime] (aside other details about the class).
 */
class ClassTimesAdapter(
        private val activity: Activity,
        private val classTimeGroups: List<ClassTimeGroup>
) : RecyclerView.Adapter<ClassTimesAdapter.ClassTimesViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ClassTimesViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_class_time, parent, false)
        return ClassTimesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClassTimesViewHolder?, position: Int) {
        val timeGroup = classTimeGroups[position]

        with(holder!!) {
            timeText.text = "${timeGroup.startTime} - ${timeGroup.endTime}"
            dayText.text = getDayText(timeGroup)
        }
    }

    private fun getDayText(timeGroup: ClassTimeGroup): String {
        val timetable = (activity.application as TimetableApplication).currentTimetable!!

        val dayTextBuilder = StringBuilder()

        val classTimes = timeGroup.classTimes
        for (i in classTimes.indices) {
            val classTime = classTimes[i]

            dayTextBuilder.append(classTime.day.toString().toLowerCase().title())

            if (!timetable.hasFixedScheduling()) {
                val weekItem = classTime.getWeekText(activity)
                dayTextBuilder.append(" ").append(weekItem)
            }

            if (i != classTimes.size - 1) dayTextBuilder.append("\n")
        }

        return dayTextBuilder.toString()
    }

    override fun getItemCount() = classTimeGroups.size

    inner class ClassTimesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val timeText = itemView.findViewById(R.id.time) as TextView
        val dayText = itemView.findViewById(R.id.day) as TextView

        init {
            itemView.setOnClickListener { view ->
                onItemClickListener?.invoke(view!!, layoutPosition)
            }
        }

    }

}
