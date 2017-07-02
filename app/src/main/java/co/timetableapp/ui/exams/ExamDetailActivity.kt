package co.timetableapp.ui.exams

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.Color
import co.timetableapp.model.Exam
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils

/**
 * Shows the details of an exam.
 *
 * @see Exam
 * @see co.timetableapp.ui.agenda.ExamsFragment
 * @see ExamEditActivity
 * @see ItemDetailActivity
 */
class ExamDetailActivity : ItemDetailActivity<Exam>() {

    override fun initializeDataHandler() = ExamHandler(this)

    override fun getLayoutResource() = R.layout.activity_exam_detail

    override fun onNullExtras() {
        val intent = Intent(this, ExamEditActivity::class.java)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun setupLayout() {
        setupToolbar()

        val dateFormatter = DateUtils.FORMATTER_FULL_DATE
        (findViewById(R.id.textView_date) as TextView).text = mItem.date.format(dateFormatter)

        val timeText =
                "${mItem.startTime} - ${mItem.startTime.plusMinutes(mItem.duration.toLong())}"
        (findViewById(R.id.textView_times) as TextView).text = timeText

        val seatGroup = findViewById(R.id.viewGroup_seat)
        if (mItem.hasSeat()) {
            seatGroup.visibility = View.VISIBLE
            (findViewById(R.id.textView_seat) as TextView).text = mItem.seat
        } else {
            seatGroup.visibility = View.GONE
        }

        val roomGroup = findViewById(R.id.viewGroup_room)
        if (mItem.hasRoom()) {
            roomGroup.visibility = View.VISIBLE
            (findViewById(R.id.textView_room) as TextView).text = mItem.room
        } else {
            roomGroup.visibility = View.GONE
        }

        findViewById(R.id.location_divider).visibility =
                if (!mItem.hasRoom() && !mItem.hasSeat()) View.GONE else View.VISIBLE

        val viewGroupResit = findViewById(R.id.viewGroup_resit)
        viewGroupResit.visibility = if (mItem.resit) View.VISIBLE else View.GONE

        val notesText = findViewById(R.id.textView_notes) as TextView
        UiUtils.formatNotesTextView(this, notesText, mItem.notes)
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        val subject = Subject.create(this, mItem.subjectId)
        (findViewById(R.id.title) as TextView).text = mItem.makeName(subject)

        val color = Color(subject.colorId)
        UiUtils.setBarColors(color, this, toolbar)
    }

    override fun onMenuEditClick() {
        val intent = Intent(this, ExamEditActivity::class.java)
        intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItem)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    override fun saveEditsAndClose() {
        val intent = Intent().putExtra(EXTRA_ITEM, mItem)
        setResult(Activity.RESULT_OK, intent) // to reload any changes in ExamsActivity
        supportFinishAfterTransition()
    }

    override fun saveDeleteAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ExamsActivity
        finish()
    }

}
