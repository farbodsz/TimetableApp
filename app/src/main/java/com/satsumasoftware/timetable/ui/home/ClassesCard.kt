package com.satsumasoftware.timetable.ui.home

import android.content.Context
import android.content.Intent
import android.view.View
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.framework.ClassTime
import com.satsumasoftware.timetable.ui.ScheduleActivity
import com.satsumasoftware.timetable.ui.adapter.HomeClassesAdapter
import java.util.*

class ClassesCard(override val context: Context, classTimes: ArrayList<ClassTime>) : ListCard {

    override val title = context.getString(R.string.home_card_classes_title)!!

    override val colorRes = R.color.mdu_light_blue_500

    override val forwardActionText = context.getString(R.string.home_card_classes_action)!!

    override val recyclerAdapter = HomeClassesAdapter(context, classTimes)

    override val isListEmpty = classTimes.isEmpty()

    override val placeholderText = context.getString(R.string.home_card_classes_placeholder)!!

    override fun getBottomBarClickListener() =  View.OnClickListener {
        context.startActivity(Intent(context, ScheduleActivity::class.java))
    }

}
