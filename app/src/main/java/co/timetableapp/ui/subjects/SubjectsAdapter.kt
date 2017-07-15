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

package co.timetableapp.ui.subjects

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.model.Color
import co.timetableapp.model.Subject
import co.timetableapp.ui.OnItemClickListener

/**
 * A RecyclerView adapter for displaying a list of subjects.
 */
class SubjectsAdapter(
        private val context: Context,
        private val subjects: List<Subject>
) : RecyclerView.Adapter<SubjectsAdapter.SubjectViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SubjectViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder?, position: Int) {
        val subject = subjects[position]
        val color = Color(subject.colorId)

        with(holder!!) {
            colorView.setBackgroundColor(
                    ContextCompat.getColor(context, color.getPrimaryColorResId(context)))
            nameText.text = subject.name
        }
    }

    override fun getItemCount() = subjects.size

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val colorView = itemView.findViewById(R.id.color)!!
        val nameText = itemView.findViewById(R.id.name) as TextView

        init {
            itemView.setOnClickListener { view ->
                onItemClickListener?.invoke(view, layoutPosition)
            }
        }

    }

}
