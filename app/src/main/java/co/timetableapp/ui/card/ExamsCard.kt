package co.timetableapp.ui.card

import android.content.Context
import android.content.Intent
import android.view.View
import co.timetableapp.R
import co.timetableapp.framework.Exam
import co.timetableapp.ui.ExamsActivity
import co.timetableapp.ui.adapter.HomeExamsAdapter
import java.util.*

class ExamsCard(override val context: Context, exams: ArrayList<Exam>) : ListCard {

    override val title = context.getString(R.string.home_card_exams_title)!!

    override val colorRes = R.color.mdu_purple_500

    override val forwardActionText = context.getString(R.string.home_card_exams_action)!!

    override val recyclerAdapter = HomeExamsAdapter(context, exams)

    override val isListEmpty = exams.isEmpty()

    override val placeholderText = ""

    override fun getBottomBarClickListener() =  View.OnClickListener {
        context.startActivity(Intent(context, ExamsActivity::class.java))
    }

}
