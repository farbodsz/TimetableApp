package com.satsumasoftware.timetable.ui.home

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.framework.Assignment
import com.satsumasoftware.timetable.framework.ClassTime
import com.satsumasoftware.timetable.ui.AssignmentsActivity
import com.satsumasoftware.timetable.ui.ScheduleActivity
import com.satsumasoftware.timetable.ui.adapter.HomeAssignmentsAdapter
import com.satsumasoftware.timetable.ui.adapter.HomeClassesAdapter
import java.util.*

class AssignmentsCard(val context: Context, val assignments: ArrayList<Assignment>) : HomeCard {

    override val title =
            context.resources.getString(R.string.home_card_assignments_title)!!
    override val forwardActionText: String? =
            context.resources.getString(R.string.home_card_assignments_action)!!

    override fun loadContent(container: ViewGroup) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.card_home_list_content, null)

        val recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView

        with(recyclerView) {
            layoutManager = object : LinearLayoutManager(context) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }

            setHasFixedSize(true)
            adapter = HomeAssignmentsAdapter(context, assignments)
        }

        container.addView(view)
    }

    override fun getBottomBarClickListener() =  View.OnClickListener {
        context.startActivity(Intent(context, AssignmentsActivity::class.java))
    }

}
