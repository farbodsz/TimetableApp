package com.satsumasoftware.timetable.ui.card

import android.content.Context
import android.content.Intent
import android.view.View
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.framework.Assignment
import com.satsumasoftware.timetable.ui.AssignmentsActivity
import com.satsumasoftware.timetable.ui.adapter.HomeAssignmentsAdapter
import java.util.*

class AssignmentsCard(override val context: Context, assignments: ArrayList<Assignment>) : ListCard {

    override val title = context.getString(R.string.home_card_assignments_title)!!

    override val colorRes = R.color.mdu_deep_orange_500

    override val forwardActionText = context.getString(R.string.home_card_assignments_action)!!

    override val recyclerAdapter = HomeAssignmentsAdapter(context, assignments)

    override val isListEmpty = assignments.isEmpty()

    override val placeholderText = context.getString(R.string.home_card_assignments_placeholder)!!

    override fun getBottomBarClickListener() =  View.OnClickListener {
        context.startActivity(Intent(context, AssignmentsActivity::class.java))
    }

}
