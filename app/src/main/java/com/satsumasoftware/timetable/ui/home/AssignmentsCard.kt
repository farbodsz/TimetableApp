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

class AssignmentsCard(override val context: Context, assignments: ArrayList<Assignment>) : ListCard {

    override val title =
            context.resources.getString(R.string.home_card_assignments_title)!!
    override val forwardActionText: String? =
            context.resources.getString(R.string.home_card_assignments_action)!!

    override val recyclerAdapter = HomeAssignmentsAdapter(context, assignments)
    override val isListEmpty = assignments.isEmpty()

    override val placeholderText: String =
            context.getString(R.string.home_card_assignments_placeholder)

    override val colorRes = R.color.mdu_deep_orange_500

    override fun getBottomBarClickListener() =  View.OnClickListener {
        context.startActivity(Intent(context, AssignmentsActivity::class.java))
    }

}
