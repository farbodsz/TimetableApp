package com.satsumasoftware.timetable.ui.home

import android.support.annotation.ColorRes
import android.view.View
import android.view.ViewGroup

interface HomeCard {

    val title: String
    val forwardActionText: String?

    val colorRes: Int

    fun loadContent(container: ViewGroup)

    fun hasForwardAction() = forwardActionText != null

    fun getBottomBarClickListener(): View.OnClickListener

}
