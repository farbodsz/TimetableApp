package com.satsumasoftware.timetable.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.db.handler.ExamHandler
import com.satsumasoftware.timetable.framework.Color
import com.satsumasoftware.timetable.framework.Exam
import com.satsumasoftware.timetable.framework.Subject
import com.satsumasoftware.timetable.ui.ExamDetailActivity.Companion.EXTRA_EXAM
import com.satsumasoftware.timetable.util.UiUtils
import org.threeten.bp.format.DateTimeFormatter

/**
 * Shows the details of an exam.
 *
 * The details are displayed to the user but they cannot be edited here and must be done in
 * [ExamEditActivity].
 *
 * Additionally, this activity should be invoked to create a new exam, passing no intent
 * data so that [EXTRA_EXAM] is null.
 *
 * @see Exam
 * @see ExamsActivity
 * @see ExamEditActivity
 */
class ExamDetailActivity : AppCompatActivity() {

    companion object {

        private const val LOG_TAG = "ExamDetailActivity"

        /**
         * The key for the [Exam] passed through an intent extra.

         * It should be null if we're creating a new exam.
         */
        internal const val EXTRA_EXAM = "extra_exam"

        private const val REQUEST_CODE_EXAM_EDIT = 1
    }

    private var mExam: Exam? = null

    private var mIsNew: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam_detail)

        val extras = intent.extras
        if (extras == null) {
            // No exam to display - assume we mean to create a new one
            mIsNew = true
            val intent = Intent(this, ExamEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_EXAM_EDIT)
            return
        }

        mExam = extras.getParcelable(EXTRA_EXAM)

        setupLayout()
    }

    private fun setupLayout() {
        setupToolbar()

        val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM uuuu")
        (findViewById(R.id.textView_date) as TextView).text = mExam!!.date.format(dateFormatter)

        val timeText =
                "${mExam!!.startTime} - ${mExam!!.startTime.plusMinutes(mExam!!.duration.toLong())}"
        (findViewById(R.id.textView_times) as TextView).text = timeText

        val seatGroup = findViewById(R.id.viewGroup_seat)
        if (mExam!!.hasSeat()) {
            seatGroup.visibility = View.VISIBLE
            (findViewById(R.id.textView_seat) as TextView).text = mExam!!.seat
        } else {
            seatGroup.visibility = View.GONE
        }

        val roomGroup = findViewById(R.id.viewGroup_room)
        if (mExam!!.hasRoom()) {
            roomGroup.visibility = View.VISIBLE
            (findViewById(R.id.textView_room) as TextView).text = mExam!!.room
        } else {
            roomGroup.visibility = View.GONE
        }

        findViewById(R.id.location_divider).visibility =
                if (!mExam!!.hasRoom() && !mExam!!.hasSeat()) View.GONE else View.VISIBLE

        val viewGroupResit = findViewById(R.id.viewGroup_resit)
        viewGroupResit.visibility = if (mExam!!.resit) View.VISIBLE else View.GONE
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        val subject = Subject.create(this, mExam!!.subjectId)!!
        supportActionBar!!.title = mExam!!.makeName(subject)

        val color = Color(subject.colorId)
        UiUtils.setBarColors(color, this, toolbar)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EXAM_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the edited exam (it would have the highest id if new)
                val editedExamId = if (mIsNew) {
                    ExamHandler(this).getHighestItemId()
                } else {
                    mExam!!.id
                }
                mExam = Exam.create(this, editedExamId)

                if (mExam == null) {
                    Log.v(LOG_TAG, "Exam is null - must have been deleted")
                    saveDeleteAndClose()
                    return
                }

                if (mIsNew) {
                    saveEditsAndClose()
                } else {
                    setupLayout()
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (mIsNew) {
                    cancelAndClose()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_detail, menu)
        UiUtils.tintMenuIcons(this, menu!!, R.id.action_edit)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_edit -> {
                val intent = Intent(this, ExamEditActivity::class.java)
                intent.putExtra(ExamEditActivity.EXTRA_EXAM, mExam)
                startActivityForResult(intent, REQUEST_CODE_EXAM_EDIT)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = saveEditsAndClose()

    private fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    private fun saveEditsAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ExamsActivity
        supportFinishAfterTransition()
    }

    private fun saveDeleteAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ExamsActivity
        finish()
    }

}
