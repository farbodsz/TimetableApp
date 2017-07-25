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
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.model.home.HomeHeader
import co.timetableapp.model.home.HomeItem
import co.timetableapp.model.home.HomeListItem
import co.timetableapp.ui.OnItemClick

/**
 * A RecyclerView adapter for displaying items on the home screen.
 */
class HomeItemsAdapter(
        private val activity: Activity,
        private val items: List<HomeListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_ITEM = 2
    }

    private var onHeaderClick: OnItemClick? = null
    private var onItemClick: OnItemClick? = null

    fun onHeaderClick(action: OnItemClick) {
        onHeaderClick = action
    }

    fun onItemClick(action: OnItemClick) {
        onItemClick = action
    }

    private inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val title: TextView = itemView.findViewById(R.id.title)

        init {
            itemView.setOnClickListener { onHeaderClick?.invoke(it!!, layoutPosition) }
        }
    }

    private inner class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val colorView: View = itemView.findViewById(R.id.color)
        val title: TextView = itemView.findViewById(R.id.title)
        val subtitle: TextView = itemView.findViewById(R.id.subtitle)
        val times: TextView = itemView.findViewById(R.id.times)
        val boxedText: TextView = itemView.findViewById(R.id.assignment_text)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(it!!, layoutPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = when(viewType) {
        VIEW_TYPE_HEADER -> {
            val headerView = LayoutInflater.from(parent!!.context)
                    .inflate(R.layout.header_list_section, parent, false)
            HeaderViewHolder(headerView)
        }
        VIEW_TYPE_ITEM -> {
            val itemView = LayoutInflater.from(parent!!.context)
                    .inflate(R.layout.item_home_card, parent, false)
            HomeViewHolder(itemView)
        }
        else -> throw IllegalArgumentException("invalid view type")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_HEADER -> setupHeaderLayout(holder as HeaderViewHolder, position)
            VIEW_TYPE_ITEM -> setupItemLayout(holder as HomeViewHolder, position)
        }
    }

    private fun setupHeaderLayout(holder: HeaderViewHolder, position: Int) {
        holder.title.text = (items[position] as HomeHeader).name
    }

    private fun setupItemLayout(holder: HomeViewHolder, position: Int) {
        val homeItem = items[position] as HomeItem

        val properties = homeItem.getHomeItemProperties(activity)

        with(holder) {
            colorView.setBackgroundColor(ContextCompat.getColor(
                    activity,
                    properties.color.getPrimaryColorResId(activity)
            ))

            title.text = properties.title
            times.text = properties.time

            properties.subtitle?.let {
                subtitle.visibility = View.VISIBLE
                subtitle.text = it
            }

            properties.extraText?.let {
                boxedText.visibility = View.VISIBLE
                boxedText.text = it
            }
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = when (items[position]) {
        is HomeHeader -> VIEW_TYPE_HEADER
        is HomeItem -> VIEW_TYPE_ITEM
        else -> throw IllegalArgumentException("invalid type at position: $position")
    }

}
