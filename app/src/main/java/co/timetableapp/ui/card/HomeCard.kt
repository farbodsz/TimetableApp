package co.timetableapp.ui.card

import android.view.View
import android.view.ViewGroup

interface HomeCard {

    val title: String

    val colorRes: Int

    val forwardActionText: String?

    fun loadContent(container: ViewGroup)

    fun hasForwardAction() = forwardActionText != null

    fun getBottomBarClickListener(): View.OnClickListener

}
