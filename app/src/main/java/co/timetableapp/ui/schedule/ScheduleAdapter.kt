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

package co.timetableapp.ui.schedule

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.model.*
import co.timetableapp.ui.OnItemClick

/**
 * A RecyclerView adapter for displaying items in the schedule page.
 */
class ScheduleAdapter(
        private val context: Context,
        private val classTimes: List<ClassTime>
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    private var onItemClick: OnItemClick? = null

    fun onItemClick(action: OnItemClick) {
        onItemClick = action
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ScheduleViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_general, parent, false)
        return ScheduleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder?, position: Int) {
        val classTime = classTimes[position]

        val classDetail = ClassDetail.create(context, classTime.classDetailId)
        val cls = Class.create(context, classDetail.classId)
        val subject = Subject.create(context, cls.subjectId)

        val color = Color(subject.colorId)

        with(holder!!) {
            colorView.setBackgroundColor(
                    ContextCompat.getColor(context, color.getPrimaryColorResId(context)))
            subjectText.text = cls.makeName(subject)
            detailText.text = makeDetailText(classDetail)
            timesText.text = "${classTime.startTime} - ${classTime.endTime}"
        }
    }

    private fun makeDetailText(classDetail: ClassDetail): String {
        val builder = StringBuilder()

        if (classDetail.hasRoom()) {
            builder.append(classDetail.room)
        }

        if (classDetail.hasBuilding()) {
            if (classDetail.hasRoom()) builder.append(", ")
            builder.append(classDetail.building)
        }

        if (classDetail.hasTeacher()) {
            if (classDetail.hasRoom() || classDetail.hasBuilding()) builder.append(" \u2022 ")
            builder.append(classDetail.teacher)
        }

        return builder.toString()
    }

    override fun getItemCount() = classTimes.size

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val colorView = itemView.findViewById(R.id.color)!!
        val subjectText = itemView.findViewById(R.id.text1) as TextView
        val detailText = itemView.findViewById(R.id.text2) as TextView
        val timesText = itemView.findViewById(R.id.text3) as TextView

        init {
            itemView.findViewById(R.id.text4).visibility = View.GONE

            itemView.setOnClickListener { onItemClick?.invoke(it, layoutPosition) }
        }

    }

}
