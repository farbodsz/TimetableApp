package co.timetableapp.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.framework.Exam
import co.timetableapp.ui.adapter.ExamsAdapter
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * An activity for displaying a list of exams to the user.
 *
 * @see Exam
 * @see ExamDetailActivity
 * @see ExamEditActivity
 */
class ExamsActivity : ItemListActivity<Exam>() {

    companion object {
        private val REQUEST_CODE_EXAM_EDIT = 1
    }

    private var mHeaders: ArrayList<String?>? = null
    private var mShowPast = false

    override fun instantiateDataHandler() = ExamHandler(this)

    override fun onFabButtonClick() {
        val intent = Intent(this, ExamEditActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_EXAM_EDIT)
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = ExamsAdapter(this, mHeaders, mItems)
        adapter.setOnEntryClickListener { view, position ->
            val intent = Intent(this, ExamDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM, mItems[position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_EXAM_EDIT, bundle)
        }

        return adapter
    }

    override fun setupList() {
        mHeaders = ArrayList<String?>()
        super.setupList()
    }

    override fun sortList() {
        Collections.sort(mItems) { e1, e2 ->
            val dateTime1 = e1.makeDateTimeObject()
            val dateTime2 = e2.makeDateTimeObject()
            if (mShowPast) {
                dateTime2.compareTo(dateTime1)
            } else {
                dateTime1.compareTo(dateTime2)
            }
        }

        val headers = ArrayList<String?>()
        val exams = ArrayList<Exam?>()

        var currentTimePeriod = -1

        for (i in mItems.indices) {
            val exam = mItems[i]

            val examDate = exam.date
            val timePeriodId: Int

            if (exam.makeDateTimeObject().isBefore(LocalDateTime.now())) {
                if (mShowPast) {
                    timePeriodId = Integer.parseInt(examDate.year.toString() + examDate.monthValue.toString())

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(examDate.format(DateTimeFormatter.ofPattern("MMMM uuuu")))
                        exams.add(null)
                    }

                    headers.add(null)
                    exams.add(exam)

                    currentTimePeriod = timePeriodId
                }

            } else {
                if (!mShowPast) {
                    timePeriodId = DateUtils.getDatePeriodId(examDate)

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(DateUtils.makeHeaderName(this, timePeriodId))
                        exams.add(null)
                    }

                    headers.add(null)
                    exams.add(exam)

                    currentTimePeriod = timePeriodId
                }
            }
        }

        mHeaders!!.clear()
        mHeaders!!.addAll(headers)

        mItems.clear()
        mItems.addAll(exams)
    }

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            this,
            R.drawable.ic_assessment_black_24dp,
            if (mShowPast) R.string.placeholder_exams_past else R.string.placeholder_exams,
            R.color.mdu_blue_400,
            R.color.mdu_white,
            R.color.mdu_white,
            true)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EXAM_EDIT) {
            if (resultCode == RESULT_OK) {
                updateList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_assignments, menu)
        menu!!.findItem(R.id.action_show_past).title = getString(R.string.action_show_past_exams)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_show_past -> {
                mShowPast = !mShowPast
                item.isChecked = mShowPast

                with(findViewById(R.id.text_infoBar) as TextView) {
                    if (mShowPast) {
                        visibility = View.VISIBLE
                        text = getString(R.string.showing_past_exams)
                    } else {
                        visibility = View.GONE
                    }
                }

                updateList()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun getSelfNavDrawerItem() = NavigationDrawerActivity.NAVDRAWER_ITEM_EXAMS

}
