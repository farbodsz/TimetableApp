package com.satsumasoftware.timetable.ui.card

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.satsumasoftware.timetable.R

interface ListCard : HomeCard {

    val context: Context
    val recyclerAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>

    val isListEmpty: Boolean
    val placeholderText: String

    override fun loadContent(container: ViewGroup) {
        val inflater = LayoutInflater.from(context)

        val view: View

        if (isListEmpty) {
            view = inflater.inflate(R.layout.card_home_list_placeholder, null)
            val textView = view.findViewById(R.id.placeholder) as TextView
            textView.text = placeholderText

        } else {
            view = inflater.inflate(R.layout.card_home_list_content, null)
            val recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView

            with(recyclerView) {
                layoutManager = object : LinearLayoutManager(context) {
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }

                setHasFixedSize(true)
                adapter = recyclerAdapter
            }
        }

        container.addView(view)
    }

}
