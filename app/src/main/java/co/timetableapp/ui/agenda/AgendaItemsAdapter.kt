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

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.model.Assignment
import co.timetableapp.model.Color
import co.timetableapp.model.Event
import co.timetableapp.model.Subject
import co.timetableapp.model.agenda.AgendaHeader
import co.timetableapp.model.agenda.AgendaItem
import co.timetableapp.model.agenda.AgendaListItem
import co.timetableapp.ui.OnItemClick
import co.timetableapp.util.DateUtils

/**
 * A RecyclerView adapter to be used for items being displayed on the "Agenda" page.
 */
class AgendaItemsAdapter(
        private val context: Context,
        private val items: List<AgendaListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_ITEM = 2
    }

    private var onItemClick: OnItemClick? = null

    fun onItemClick(action: OnItemClick) {
        onItemClick = action
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text)
    }

    inner class AgendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val colorView: View = itemView.findViewById(R.id.color)
        val title: TextView = itemView.findViewById(R.id.text1)
        val subtitle: TextView = itemView.findViewById(R.id.text2)
        val info1: TextView = itemView.findViewById(R.id.text3)
        val info2: TextView = itemView.findViewById(R.id.text4)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(it, layoutPosition) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val headerView = LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.subheader, parent, false)
                HeaderViewHolder(headerView)
            }
            VIEW_TYPE_ITEM -> {
                val itemView = LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.item_general, parent, false)
                AgendaViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_HEADER -> setupHeaderLayout(holder as HeaderViewHolder, position)
            VIEW_TYPE_ITEM -> setupItemLayout(holder as AgendaViewHolder, position)
        }
    }

    private fun setupHeaderLayout(holder: HeaderViewHolder, position: Int) {
        holder.textView.text = (items[position] as AgendaHeader).getName(context)
    }

    private fun setupItemLayout(holder: AgendaViewHolder, position: Int) {
        val agendaItem = items[position] as AgendaItem

        val relatedSubject = agendaItem.getRelatedSubject(context)

        with(holder) {
            title.text = agendaItem.getDisplayedTitle()
            subtitle.text = makeSubtitleText(agendaItem, relatedSubject)

            info1.text = agendaItem.getDateTime().toLocalDate()
                    .format(DateUtils.FORMATTER_FULL_DATE) // TODO display start **and end** dates

            if (agendaItem is Assignment) {
                // Has no time to display, but has completion progress
                info2.text = "${agendaItem.completionProgress}%"
            } else {
                info2.text = agendaItem.getDateTime().toLocalTime().format(DateUtils.FORMATTER_TIME)
            }

            val color = if (relatedSubject == null) {
                Event.DEFAULT_COLOR
            } else {
                Color(relatedSubject.colorId)
            }
            colorView.setBackgroundColor(
                    ContextCompat.getColor(context, color.getPrimaryColorResId(context)))
        }
    }

    /**
     * @return a string to be used as a subtitle on the agenda list item
     */
    private fun makeSubtitleText(agendaItem: AgendaItem, relatedSubject: Subject?): String {
        var subtitleText = ""
        relatedSubject?.let { subtitleText += "${it.name} " }
        subtitleText += context.getString(agendaItem.getTypeNameRes())

        return subtitleText
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = when (items[position]) {
        is AgendaHeader -> VIEW_TYPE_HEADER
        is AgendaItem -> VIEW_TYPE_ITEM
        else -> throw IllegalArgumentException("invalid item type at position: $position")
    }

}
