package co.timetableapp.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.data.handler.EventHandler
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.*
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.ui.components.SectionGroup
import co.timetableapp.ui.events.EventEditActivity
import co.timetableapp.ui.exams.ExamDetailActivity
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * This page displays the user's upcoming classes and exams.
 *
 * 'Upcoming' is defined to mean within the next week.
 *
 * @see MainActivity
 * @see TodayFragment
 */
class UpcomingFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "UpcomingFragment"
    }

    private var mSectionContainer: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_home_main, container, false)

        mSectionContainer = rootView.findViewById(R.id.section_container) as LinearLayout
        setupLayout()

        return rootView
    }

    private fun setupLayout() {
        val inflater = LayoutInflater.from(context)

        setupExamSection(inflater)
        setupAssignmentSection(inflater)
        setupEventSection(inflater)
    }

    private fun setupExamSection(inflater: LayoutInflater) {
        val exams = getUpcomingExams()
        if (exams.isNotEmpty()) {
            val examsSection = SectionGroup.Builder(context, mSectionContainer!!)
                    .setTitle(R.string.title_exams)
                    .build()
            addExamCards(examsSection.containerView, inflater, exams)

            mSectionContainer!!.addView(examsSection.view)
        }
    }

    /**
     * @return a list of exams due between today's date and next week.
     */
    private fun getUpcomingExams(): ArrayList<Exam> {
        val upcomingExams = ArrayList<Exam>()
        val now = LocalDate.now()
        val upperDate = now.plusWeeks(1)

        ExamHandler(context).getItems(activity.application).forEach {
            if (it.date.isAfter(now) && it.date.isBefore(upperDate)) {
                upcomingExams.add(it)
            }
        }

        return upcomingExams
    }

    private fun addExamCards(container: ViewGroup, inflater: LayoutInflater,
                             exams: ArrayList<Exam>) {
        if (exams.isEmpty()) {
            val card = inflater.inflate(R.layout.item_empty_placeholder, container, false)
            container.addView(card)
            return
        }

        for (exam in exams.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val subject = Subject.create(context, exam.subjectId)!!
            val color = Color(subject.colorId)

            val formatter = DateTimeFormatter.ofPattern("EEE\nHH:mm")
            val datesText = exam.makeDateTimeObject().format(formatter).toUpperCase()

            with(card) {
                findViewById(R.id.color).setBackgroundColor(
                        ContextCompat.getColor(context, color.getPrimaryColorResId(context)))

                (findViewById(R.id.title) as TextView).text = exam.makeName(subject)
                (findViewById(R.id.subtitle) as TextView).text = exam.formatLocationText()
                (findViewById(R.id.times) as TextView).text = datesText

                setOnClickListener {
                    val intent = Intent(activity, ExamDetailActivity::class.java)
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM, exam)
                    startActivity(intent)
                }
            }

            container.addView(card)
        }
    }

    private fun setupAssignmentSection(inflater: LayoutInflater) {
        val assignmentSection = SectionGroup.Builder(context, mSectionContainer!!)
                .setTitle(R.string.title_assignments)
                .build()
        addAssignmentCards(assignmentSection.containerView, inflater, getUpcomingAssignments())
        mSectionContainer!!.addView(assignmentSection.view)
    }

    /**
     * @return a list of assignments due between today's date and next week.
     */
    private fun getUpcomingAssignments(): ArrayList<Assignment> {
        val upcomingAssignments = ArrayList<Assignment>()
        val upperDate = LocalDate.now().plusWeeks(1)

        AssignmentHandler(context).getItems(activity.application).forEach {
            if (it.isUpcoming() && it.dueDate.isBefore(upperDate)) {
                upcomingAssignments.add(it)
            }
        }

        return upcomingAssignments
    }

    private fun addAssignmentCards(container: ViewGroup, inflater: LayoutInflater,
                                    assignments: ArrayList<Assignment>) {
        if (assignments.isEmpty()) {
            val card = inflater.inflate(R.layout.item_empty_placeholder, container, false)
            container.addView(card)
            return
        }

        for (assignment in assignments.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val cls = Class.create(context, assignment.classId)!!
            val subject = Subject.create(context, cls.subjectId)!!
            val color = Color(subject.colorId)

            val formatter = DateTimeFormatter.ofPattern("EEE\nd")
            val datesText = assignment.dueDate.format(formatter).toUpperCase()

            with(card) {
                findViewById(R.id.color).setBackgroundColor(
                        ContextCompat.getColor(context, color.getPrimaryColorResId(context)))

                (findViewById(R.id.title) as TextView).text = assignment.title
                (findViewById(R.id.subtitle) as TextView).text = cls.makeName(subject)
                (findViewById(R.id.times) as TextView).text = datesText

                setOnClickListener {
                    val intent = Intent(context, AssignmentDetailActivity::class.java)
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM, assignment)
                    startActivityForResult(intent, MainActivity.REQUEST_CODE_ITEM_DETAIL)
                }
            }

            container.addView(card)
        }
    }

    private fun setupEventSection(inflater: LayoutInflater) {
        val assignmentSection = SectionGroup.Builder(context, mSectionContainer!!)
                .setTitle(R.string.title_events)
                .build()
        addEventCards(assignmentSection.containerView, inflater, getUpcomingEvents())
        mSectionContainer!!.addView(assignmentSection.view)
    }

    private fun getUpcomingEvents(): ArrayList<Event> {
        val upcomingEvents = ArrayList<Event>()
        val upperDate = LocalDate.now().plusWeeks(1)

        EventHandler(context).getItems(activity.application).forEach {
            if (it.isUpcoming() && it.startTime.toLocalDate().isBefore(upperDate)) {
                upcomingEvents.add(it)
            }
        }

        return upcomingEvents
    }

    private fun addEventCards(container: ViewGroup, inflater: LayoutInflater,
                              events: ArrayList<Event>) {
        if (events.isEmpty()) {
            return
        }

        for (event in events.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val formatter = DateTimeFormatter.ofPattern("EEE\nHH:mm")
            val datesText = event.startTime.format(formatter).toUpperCase()

            with(card) {
                findViewById(R.id.color).setBackgroundColor(ContextCompat.getColor(
                        context,
                        Event.DEFAULT_COLOR.getPrimaryColorResId(context)))

                (findViewById(R.id.title) as TextView).text = event.title
                (findViewById(R.id.times) as TextView).text = datesText

                setOnClickListener {
                    val intent = Intent(activity, EventEditActivity::class.java) // TODO use EventDetailActivity once added
                    intent.putExtra(ItemEditActivity.EXTRA_ITEM, event) // TODO and then ItemDetailActivity here
                    startActivity(intent)
                }
            }

            container.addView(card)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.REQUEST_CODE_ITEM_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(LOG_TAG, "UpcomingFragment: received activity result - refreshing lists")
                mSectionContainer!!.removeAllViews()
                setupLayout()
            }
        }
    }

}
