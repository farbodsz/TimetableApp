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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.model.Term
import co.timetableapp.ui.OnItemClick
import co.timetableapp.util.DateUtils

/**
 * A RecyclerView adapter for displaying a list of timetables.
 */
class TermsAdapter(
        private val terms: List<Term>
) : RecyclerView.Adapter<TermsAdapter.TermViewHolder>() {

    private var onItemClick: OnItemClick? = null

    fun onItemClick(action: OnItemClick) {
        onItemClick = action
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): TermViewHolder {
        val itemView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.item_class_time, parent, false)
        return TermViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TermViewHolder?, position: Int) {
        val term = terms[position]

        val formatter = DateUtils.FORMATTER_FULL_DATE
        val dates = "${term.startDate.format(formatter)} - ${term.endDate.format(formatter)}"

        with(holder!!) {
            nameText.text = term.name
            detailText.text = dates
        }
    }

    override fun getItemCount() = terms.size

    inner class TermViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nameText: TextView = itemView.findViewById(R.id.time)
        val detailText: TextView = itemView.findViewById(R.id.day)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(it, layoutPosition) }
        }

    }

}
