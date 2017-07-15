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

package co.timetableapp.ui.timetables

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.model.Timetable
import co.timetableapp.ui.OnItemClickListener
import co.timetableapp.util.DateUtils

/**
 * A RecyclerView adapter for displaying a list of timetables.
 */
class TimetablesAdapter(
        private val activity: Activity,
        private val timetables: List<Timetable>,
        private val rootView: View
) : RecyclerView.Adapter<TimetablesAdapter.TimetableViewHolder>() {

    private val application = activity.application

    private var bindingVH = false

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TimetableViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_timetable, parent, false)
        return TimetableViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TimetableViewHolder?, position: Int) {
        bindingVH = true

        val timetable = timetables[position]

        val formatter = DateUtils.FORMATTER_MONTH_YEAR
        val details = "${timetable.startDate.format(formatter)} - ${timetable.endDate.format(formatter)}"

        val currentTimetable = (application as TimetableApplication).currentTimetable!!
        val isThisTheCurrent = currentTimetable.id == timetable.id

        with(holder!!) {
            nameText.text = timetable.displayedName
            detailText.text = details

            radioButton.isChecked = isThisTheCurrent
        }

        bindingVH = false
    }

    override fun getItemCount() = timetables.size

    inner class TimetableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val radioButton = itemView.findViewById(R.id.radioButton) as RadioButton
        val nameText = itemView.findViewById(R.id.name) as TextView
        val detailText = itemView.findViewById(R.id.details) as TextView

        init {
            itemView.setOnClickListener { view ->
                onItemClickListener?.invoke(view, layoutPosition)
            }

            radioButton.setOnCheckedChangeListener { _, _ ->
                if (bindingVH) {
                    return@setOnCheckedChangeListener
                }

                val timetable = timetables[layoutPosition]
                val application = application as TimetableApplication
                application.setCurrentTimetable(activity, timetable)
                notifyDataSetChanged()

                val snackbarText = activity.getString(
                        R.string.message_set_current_timetable,
                        timetable.displayedName)
                Snackbar.make(rootView, snackbarText, Snackbar.LENGTH_SHORT).show()
            }
        }

    }

}
