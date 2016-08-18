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
import com.satsumasoftware.timetable.framework.ClassTime
import com.satsumasoftware.timetable.ui.ScheduleActivity
import com.satsumasoftware.timetable.ui.adapter.HomeClassesAdapter
import java.util.*

class ClassesCard(override val context: Context, classTimes: ArrayList<ClassTime>) : ListCard {

    override val title =
            context.resources.getString(R.string.home_card_classes_title)!!
    override val forwardActionText: String? =
            context.resources.getString(R.string.home_card_classes_action)!!

    override val recyclerAdapter = HomeClassesAdapter(context, classTimes)
    override val isListEmpty = classTimes.isEmpty()

    override val placeholderText: String =
            context.getString(R.string.home_card_classes_placeholder)

    override val colorRes = R.color.mdu_light_blue_500

    override fun getBottomBarClickListener() =  View.OnClickListener {
        context.startActivity(Intent(context, ScheduleActivity::class.java))
    }

}
