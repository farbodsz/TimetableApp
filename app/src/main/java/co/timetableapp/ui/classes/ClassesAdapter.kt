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

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.ClassDetailHandler
import co.timetableapp.model.Class
import co.timetableapp.model.Color
import co.timetableapp.model.Subject
import co.timetableapp.ui.OnItemClick

/**
 * A RecyclerView adapter for displaying a list of [classes][Class].
 */
class ClassesAdapter(
        private val context: Context,
        private val classes: List<Class>
) : RecyclerView.Adapter<ClassesAdapter.ClassesViewHolder>() {

    private var onItemClick: OnItemClick? = null

    fun onItemClick(action: OnItemClick) {
        onItemClick = action
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ClassesViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_classes, parent, false)
        return ClassesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClassesViewHolder?, position: Int) {
        val cls = classes[position]

        val subject = Subject.create(context, cls.subjectId)
        val color = Color(subject.colorId)

        with(holder!!) {
            colorView.setBackgroundColor(
                    ContextCompat.getColor(context, color.getPrimaryColorResId(context)))
            subjectText.text = cls.makeName(subject)
            detailText.text = makeDetailText(cls)
        }
    }

    private fun makeDetailText(cls: Class): String {
        val classDetails = ClassDetailHandler.getClassDetailsForClass(context, cls.id)

        val builder = StringBuilder()

        for (i in classDetails.indices) {
            val classDetail = classDetails[i]

            if (classDetail.hasRoom()) {
                builder.append(classDetail.room)
                if (classDetail.hasTeacher()) builder.append(" \u2022 ")
            }

            if (classDetail.hasTeacher()) {
                builder.append(classDetail.teacher)
            }

            if (i != classDetails.size - 1) {
                builder.append("\n")
            }
        }

        return builder.toString()
    }

    override fun getItemCount() = classes.size

    inner class ClassesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val colorView: View = itemView.findViewById(R.id.color)
        val subjectText: TextView = itemView.findViewById(R.id.subject)
        val detailText: TextView = itemView.findViewById(R.id.class_details)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(it, layoutPosition) }
        }
    }

}
