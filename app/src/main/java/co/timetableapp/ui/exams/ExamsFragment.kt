package co.timetableapp.ui.exams

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.Exam
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemListFragment
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * A fragment for displaying a list of exams to the user.
 *
 * @see Exam
 * @see ExamsActivity
 * @see ExamDetailActivity
 * @see ExamEditActivity
 */
class ExamsFragment : ItemListFragment<Exam>() {

    companion object {
        private const val REQUEST_CODE_EXAM_EDIT = 1
    }

    private var mHeaders: ArrayList<String?>? = null

    private var mShowPast = false

    override fun instantiateDataHandler() = ExamHandler(activity)

    override fun onFabButtonClick() {
        val intent = Intent(activity, ExamEditActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_EXAM_EDIT)
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = ExamsAdapter(activity, mHeaders, mItems)
        adapter.setOnEntryClickListener { view, position ->
            val intent = Intent(activity, ExamDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM, mItems!![position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            startActivityForResult(intent, REQUEST_CODE_EXAM_EDIT, bundle)
        }

        return adapter
    }

    override fun setupList() {
        mHeaders = ArrayList<String?>()
        super.setupList()
    }

    override fun sortList() {
        if (mShowPast) {
            mItems!!.sortWith(Exam.COMPARATOR_REVERSE_DATE_TIME)
        } else {
            mItems!!.sort()
        }

        val headers = ArrayList<String?>()
        val exams = ArrayList<Exam?>()

        var currentTimePeriod = -1

        for (i in mItems!!.indices) {
            val exam = mItems!![i]

            val examDate = exam.date
            val timePeriodId: Int

            if (exam.makeDateTimeObject().isBefore(LocalDateTime.now())) {
                if (mShowPast) {
                    timePeriodId = Integer.parseInt(examDate.year.toString() +
                            examDate.monthValue.toString())

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
                        headers.add(DateUtils.makeHeaderName(activity, timePeriodId))
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

        mItems!!.clear()
        mItems!!.addAll(exams)
    }

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            activity,
            R.drawable.ic_assessment_black_24dp,
            if (mShowPast) R.string.placeholder_exams_past else R.string.placeholder_exams,
            R.color.mdu_blue_400,
            R.color.mdu_white,
            R.color.mdu_white,
            true)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EXAM_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.menu_assignments, menu)
        menu!!.findItem(R.id.action_show_past).title = getString(R.string.action_show_past_exams)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_show_past -> {
                mShowPast = !mShowPast
                item.isChecked = mShowPast

                with(activity.findViewById(R.id.text_infoBar) as TextView) {
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

}
