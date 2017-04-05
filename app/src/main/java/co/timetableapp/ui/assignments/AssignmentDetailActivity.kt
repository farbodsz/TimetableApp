package co.timetableapp.ui.assignments

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.widget.SeekBar
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.model.Assignment
import co.timetableapp.model.Class
import co.timetableapp.model.Color
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.util.UiUtils
import org.threeten.bp.format.DateTimeFormatter

/**
 * Shows the details of an assignment.
 *
 * @see Assignment
 * @see AssignmentsFragment
 * @see AssignmentEditActivity
 * @see ItemDetailActivity
 */
class AssignmentDetailActivity : ItemDetailActivity<Assignment>() {

    override fun initializeDataHandler() = AssignmentHandler(this)

    override fun getLayoutResource() = R.layout.activity_assignment_detail

    override fun onNullExtras() {
        val intent = Intent(this, AssignmentEditActivity::class.java)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun setupLayout() {
        setupToolbar()

        val dateFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu")
        (findViewById(R.id.textView_date) as TextView).text = mItem!!.dueDate.format(dateFormatter)

        val progressText = findViewById(R.id.textView_progress) as TextView
        progressText.text = getString(R.string.property_progress, mItem!!.completionProgress)

        val seekBar = findViewById(R.id.seekBar) as SeekBar
        with(seekBar) {
            max = 20 // so it goes up in 5s
            progress = mItem!!.completionProgress / 5

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    mItem!!.completionProgress = progress * 5
                    progressText.text =
                            getString(R.string.property_progress, mItem!!.completionProgress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}

                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }

        val detailText = findViewById(R.id.textView_detail) as TextView
        with(detailText) {
            if (mItem!!.hasDetail()) {
                text = mItem!!.detail

                setTypeface(null, Typeface.NORMAL)
                setTextColor(ContextCompat.getColor(context, R.color.mdu_text_black))
            } else {
                text = getString(R.string.placeholder_detail_empty)

                setTypeface(null, Typeface.ITALIC)
                setTextColor(ContextCompat.getColor(context, R.color.mdu_text_black_secondary))
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        val cls = Class.create(this, mItem!!.classId)!!
        val subject = Subject.create(this, cls.subjectId)!!

        with(supportActionBar!!) {
            title = mItem!!.title
            subtitle = subject.name
        }

        val color = Color(subject.colorId)
        UiUtils.setBarColors(color, this, toolbar)
    }

    override fun onMenuEditClick() {
        val intent = Intent(this, AssignmentEditActivity::class.java)
        intent.putExtra(AssignmentEditActivity.EXTRA_ASSIGNMENT, mItem)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    override fun saveEditsAndClose() {
        // Overwrite db values as completionProgress may have changed
        mDataHandler!!.replaceItem(mItem!!.id, mItem!!)

        setResult(Activity.RESULT_OK) // to reload any changes in AssignmentsActivity
        supportFinishAfterTransition()
    }

    override fun saveDeleteAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in AssignmentsActivity
        finish()
    }

}
