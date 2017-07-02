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
import co.timetableapp.model.agenda.AgendaHeader
import co.timetableapp.model.agenda.AgendaItem
import co.timetableapp.model.agenda.AgendaListItem
import co.timetableapp.util.DateUtils

/**
 * A RecyclerView adapter to be used for items being displayed on the "Agenda" page.
 */
class AgendaListItemAdapter(
        private val context: Context,
        private val items: List<AgendaListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_ITEM = 2
    }

    private var mOnEntryClickListener: OnEntryClickListener? = null

    interface OnEntryClickListener {
        fun onEntryClick(view: View, position: Int)
    }

    fun setOnEntryClickListener(onEntryClickListener: OnEntryClickListener) {
        mOnEntryClickListener = onEntryClickListener
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.text) as TextView
    }

    inner class AgendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val colorView: View
        val title: TextView
        val subtitle: TextView
        val info1: TextView
        val info2: TextView

        init {
            itemView.setOnClickListener(this)
            colorView = itemView.findViewById(R.id.color)
            title = itemView.findViewById(R.id.text1) as TextView
            subtitle = itemView.findViewById(R.id.text2) as TextView
            info1 = itemView.findViewById(R.id.text3) as TextView
            info2 = itemView.findViewById(R.id.text4) as TextView
        }

        override fun onClick(view: View?) {
            if (mOnEntryClickListener != null) {
                mOnEntryClickListener!!.onEntryClick(view!!, layoutPosition)
            }
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
            subtitle.text = relatedSubject?.name ?: ""

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

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = when (items[position]) {
        is AgendaHeader -> VIEW_TYPE_HEADER
        is AgendaItem -> VIEW_TYPE_ITEM
        else -> throw IllegalArgumentException("invalid item type at position: $position")
    }

}
