package co.timetableapp.ui.agenda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.RecyclerView
import co.timetableapp.R
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.Exam
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemListFragment
import co.timetableapp.ui.exams.ExamDetailActivity
import co.timetableapp.ui.exams.ExamEditActivity
import co.timetableapp.ui.exams.ExamsAdapter
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils

/**
 * A fragment for displaying a list of exams to the user.
 *
 * @see Exam
 * @see AgendaActivity
 * @see ExamDetailActivity
 * @see ExamEditActivity
 */
class ExamsFragment : ItemListFragment<Exam>(), AgendaActivity.OnFilterChangeListener {

    companion object {
        private const val REQUEST_CODE_EXAM_EDIT = 3
    }

    private val mHeaders = ArrayList<String?>()

    private var mShowPast = false

    override fun instantiateDataHandler() = ExamHandler(activity)

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

            if (exam.isInPast()) {
                if (mShowPast) {
                    timePeriodId = Integer.parseInt(examDate.year.toString() +
                            examDate.monthValue.toString())

                    if (currentTimePeriod == -1 || currentTimePeriod != timePeriodId) {
                        headers.add(examDate.format(DateUtils.FORMATTER_FULL_MONTH_YEAR))
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

        mHeaders.clear()
        mHeaders.addAll(headers)

        mItems!!.clear()
        mItems!!.addAll(exams)
    }

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            activity,
            R.drawable.ic_assessment_black_24dp,
            if (mShowPast) R.string.placeholder_exams_past else R.string.placeholder_exams,
            subtitleRes = if (mShowPast)
                R.string.placeholder_exams_past_subtitle
            else
                R.string.placeholder_exams_subtitle)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AgendaActivity.REQUEST_CODE_CREATE_ITEM ||
                requestCode == REQUEST_CODE_EXAM_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
    }

    override fun onFilterChange(showCompleted: Boolean, showPast: Boolean) {
        mShowPast = showPast
        updateList()
    }

}
