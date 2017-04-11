package co.timetableapp.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
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
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.ExamsSchema
import co.timetableapp.model.*
import co.timetableapp.ui.assignments.AgendaActivity
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.classes.ClassDetailActivity
import co.timetableapp.ui.components.SectionGroup
import co.timetableapp.ui.exams.ExamDetailActivity
import co.timetableapp.util.DateUtils
import org.threeten.bp.LocalDate

/**
 * This page displays the user's classes and exams for today.
 *
 * @see MainActivity
 * @see UpcomingFragment
 */
class TodayFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "TodayFragment"
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
        val timetableId = (activity.application as TimetableApplication).currentTimetable!!.id

        val inflater = LayoutInflater.from(context)

        val classesSection = SectionGroup.Builder(context, mSectionContainer!!)
                .setTitle(R.string.title_activity_classes)
                .build()
        addClassesCards(classesSection.containerView, inflater, getClassesToday())

        mSectionContainer!!.addView(classesSection.view)

        val overdueAssignments = getOverdueAssignments()
        if (overdueAssignments.isNotEmpty()) {
            val otherSection = SectionGroup.Builder(context, mSectionContainer!!)
                    .setTitle(R.string.title_other_notices)
                    .build()

            if (overdueAssignments.isNotEmpty()) {
                addOverdueAssignmentsCard(otherSection.containerView, inflater, overdueAssignments)
            }

            mSectionContainer!!.addView(otherSection.view)
        }

        val exams = getExamsToday(timetableId)
        if (exams.isNotEmpty()) {
            val examsSection = SectionGroup.Builder(context, mSectionContainer!!)
                    .setTitle(R.string.title_exams)
                    .build()
            addExamsCards(examsSection.containerView, inflater, exams)

            mSectionContainer!!.addView(examsSection.view)
        }
    }

    private fun getClassesToday(): ArrayList<ClassTime> {
        val now = LocalDate.now()
        val today = now.dayOfWeek
        val weekNumber = DateUtils.findWeekNumber(activity.application)

        val classesToday = ArrayList<ClassTime>()
        ClassTimeHandler(activity).getItems(activity.application).forEach {
            if (it.day == today && it.weekNumber == weekNumber) {
                val classDetail = ClassDetail.create(context, it.classDetailId)
                val cls = Class.create(context, classDetail.classId)!!
                if (cls.isCurrent()) {
                    classesToday.add(it)
                }
            }
        }

        return classesToday
    }

    private fun getOverdueAssignments(): ArrayList<Assignment> {
        val overdueAssignments = ArrayList<Assignment>()

        AssignmentHandler(context).getItems(activity.application).forEach {
            if (it.isOverdue()) {
                overdueAssignments.add(it)
            }
        }

        return overdueAssignments
    }

    private fun getExamsToday(timetableId: Int): ArrayList<Exam> {
        val today = LocalDate.now()

        val query = Query.Builder()
                .addFilter(Filters.equal(ExamsSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .addFilter(Filters.equal(ExamsSchema.COL_DATE_DAY_OF_MONTH, today.dayOfMonth.toString()))
                .addFilter(Filters.equal(ExamsSchema.COL_DATE_MONTH, today.monthValue.toString()))
                .addFilter(Filters.equal(ExamsSchema.COL_DATE_YEAR, today.year.toString()))
                .build()

        return ExamHandler(activity).getAllItems(query)
    }

    private fun addClassesCards(container: ViewGroup, inflater: LayoutInflater,
                                classTimes: ArrayList<ClassTime>) {
        if (classTimes.isEmpty()) {
            val card = inflater.inflate(R.layout.item_empty_placeholder, container, false)
            container.addView(card)
            return
        }

        val assignmentsToday = getAssignmentsToday()

        for (classTime in classTimes.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val classDetail = ClassDetail.create(context, classTime.classDetailId)
            val cls = Class.create(context, classDetail.classId)!!
            val subject = Subject.create(context, cls.subjectId)!!
            val color = Color(subject.colorId)

            val classTimesText =
                    classTime.startTime.toString() + "\n" + classTime.endTime.toString()

            val classDetailBuilder = StringBuilder()
            classDetailBuilder.append(classDetail.formatLocationName())
            if (classDetail.hasTeacher()) {
                classDetailBuilder.append(" \u2022 ")
                        .append(classDetail.teacher)
            }

            with(card) {
                findViewById(R.id.color).setBackgroundColor(
                        ContextCompat.getColor(context, color.getPrimaryColorResId(context)))

                (findViewById(R.id.title) as TextView).text = cls.makeName(subject)
                (findViewById(R.id.subtitle) as TextView).text = classDetailBuilder.toString()
                (findViewById(R.id.times) as TextView).text = classTimesText

                setupAssignmentText(this, assignmentsToday, cls)

                setOnClickListener {
                    val intent = Intent(activity, ClassDetailActivity::class.java)
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM, cls)
                    startActivity(intent)
                }
            }

            container.addView(card)
        }
    }

    private fun getAssignmentsToday(): ArrayList<Assignment> {
        val today = LocalDate.now()
        val assignmentsToday = ArrayList<Assignment>()

        AssignmentHandler(context).getItems(activity.application).forEach {
            if (it.dueDate == today) {
                assignmentsToday.add(it)
            }
        }

        return assignmentsToday
    }

    private fun setupAssignmentText(card: View, assignmentsToday: ArrayList<Assignment>,
                                    cls: Class) {
        val assignmentText = card.findViewById(R.id.assignment_text) as TextView
        val classAssignments = getAssignmentsForClass(assignmentsToday, cls)

        val numberDue = classAssignments.size

        if (numberDue == 0) {
            assignmentText.visibility = View.GONE
        } else {
            assignmentText.visibility = View.VISIBLE
            assignmentText.text = getString(R.string.class_card_assignment_text, numberDue)
        }
    }

    private fun getAssignmentsForClass(assignments: ArrayList<Assignment>,
                                       cls: Class): ArrayList<Assignment> {
        val classAssignments = ArrayList<Assignment>()
        assignments.filterTo(classAssignments) { it.classId == cls.id }
        return classAssignments
    }

    private fun addOverdueAssignmentsCard(container: ViewGroup, inflater: LayoutInflater,
                                          overdueAssignments: ArrayList<Assignment>) {
        val card = inflater.inflate(R.layout.item_home_card_no_date, container, false)

        val numOverdue = overdueAssignments.size

        with(card) {
            findViewById(R.id.color).setBackgroundColor(
                    ContextCompat.getColor(context, R.color.mdu_red_900))

            with(findViewById(R.id.title) as TextView) {
                text = resources.getQuantityString(
                        R.plurals.assignments_overdue_text,
                        numOverdue,
                        numOverdue)

                setTextColor(ContextCompat.getColor(activity, R.color.mdu_red_900))
                setTypeface(null, Typeface.BOLD)
            }

            with(findViewById(R.id.subtitle) as TextView) {
                text = if (numOverdue == 1) {
                    overdueAssignments[0].title
                } else {
                    // Add the first 4 assignments or less (if there aren't 4 assignments)
                    // Note that in the UI, only up to one line will be displayed.
                    val stringBuilder = StringBuilder()
                    (0..numOverdue - 1)
                            .takeWhile { it < 4 }
                            .forEach {
                                stringBuilder.append(overdueAssignments[it].title).append(", ")
                            }
                    stringBuilder.removeSuffix(", ").toString()
                }
            }

            setOnClickListener {
                if (numOverdue == 1) {
                    val intent = Intent(activity, AssignmentDetailActivity::class.java)
                            .putExtra(ItemDetailActivity.EXTRA_ITEM, overdueAssignments[0])
                    startActivityForResult(intent, MainActivity.REQUEST_CODE_ITEM_DETAIL)
                } else {
                    val intent = Intent(activity, AgendaActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        container.addView(card)
    }

    private fun addExamsCards(container: ViewGroup, inflater: LayoutInflater,
                              exams: ArrayList<Exam>) {
        for (exam in exams.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val subject = Subject.create(context, exam.subjectId)!!
            val color = Color(subject.colorId)

            val endTime = exam.startTime.plusMinutes(exam.duration.toLong())
            val timesText =
                    exam.startTime.toString() + "\n" + endTime.toString()

            with(card) {
                findViewById(R.id.color).setBackgroundColor(
                        ContextCompat.getColor(context, color.getPrimaryColorResId(context)))

                (findViewById(R.id.title) as TextView).text = exam.makeName(subject)

                (findViewById(R.id.times) as TextView).text = timesText

                setOnClickListener {
                    val intent = Intent(activity, ExamDetailActivity::class.java)
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM, exam)
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
                Log.d(LOG_TAG, "TodayFragment: received activity result - refreshing lists")
                mSectionContainer!!.removeAllViews()
                setupLayout()
            }
        }
    }

}
