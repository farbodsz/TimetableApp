package co.timetableapp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
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
import co.timetableapp.framework.*
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.assignments.AssignmentsActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.NavigationDrawerActivity
import co.timetableapp.ui.classes.ClassDetailActivity
import co.timetableapp.ui.exams.ExamDetailActivity
import co.timetableapp.util.DateUtils
import co.timetableapp.util.SectionUi
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * The main screen showing an overview of the user's classes, assignments and exams.
 *
 * The UI is divided into two pages: one for today, and another for upcoming events. The user can
 * navigate between the pages using tabs.
 */
class MainActivity : NavigationDrawerActivity() {

    companion object {

        private const val LOG_TAG = "MainActivity"

        private const val REQUEST_CODE_ITEM_DETAIL = 1

        @JvmStatic private var sFab: FloatingActionButton? = null
    }

    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupLayout()
    }

    private fun setupLayout() {
        setupFab()

        mViewPager = findViewById(R.id.viewPager) as ViewPager
        mViewPager!!.adapter = PagerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabLayout) as TabLayout
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white))

        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(mViewPager)
    }

    private fun setupFab() {
        sFab = findViewById(R.id.fab) as FloatingActionButton
        sFab!!.setOnClickListener {
            val intent = Intent(this, AssignmentDetailActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        supportFragmentManager.fragments.forEach {
            // To update content in each fragment
            it.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_HOME

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

    private inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TodayFragment()
                1 -> UpcomingFragment()
                else -> throw IllegalArgumentException("invalid position: $position")
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return getString(when (position) {
                0 -> R.string.tab_today
                1 -> R.string.tab_upcoming
                else -> throw IllegalArgumentException("invalid position: $position")
            })
        }

    }

    /**
     * This page displays the user's classes and exams for today.
     */
    class TodayFragment : Fragment() {

        private var mSectionContainer: LinearLayout? = null

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_home_main, container, false)

            mSectionContainer = rootView.findViewById(R.id.section_container) as LinearLayout
            setupLayout()

            return rootView
        }

        override fun setUserVisibleHint(isVisibleToUser: Boolean) {
            super.setUserVisibleHint(isVisibleToUser)

            if (isVisibleToUser) {
                sFab!!.show()
            } else {
                sFab!!.hide()
            }
        }

        private fun setupLayout() {
            val timetableId = (activity.application as TimetableApplication).currentTimetable!!.id

            val inflater = LayoutInflater.from(context)

            val classesSection = SectionUi.Builder(context, mSectionContainer!!)
                    .setTitle(R.string.title_activity_classes)
                    .build()
            addClassesCards(classesSection.containerView, inflater, getClassesToday())

            mSectionContainer!!.addView(classesSection.view)

            val assignments = getAssignmentsToday()
            val overdueAssignments = getOverdueAssignments()
            if (assignments.isNotEmpty() || overdueAssignments.isNotEmpty()) {
                val assignmentsSection = SectionUi.Builder(context, mSectionContainer!!)
                        .setTitle(R.string.title_activity_assignments)
                        .build()

                if (overdueAssignments.isNotEmpty()) {
                    addOverdueAssignmentsCard(
                            assignmentsSection.containerView, inflater, overdueAssignments)
                }

                if (assignments.isNotEmpty()) {
                    addAssignmentCards(assignmentsSection.containerView, inflater, assignments)
                }

                mSectionContainer!!.addView(assignmentsSection.view)
            }

            val exams = getExamsToday(timetableId)
            if (exams.isNotEmpty()) {
                val examsSection = SectionUi.Builder(context, mSectionContainer!!)
                        .setTitle(R.string.title_activity_exams)
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

        // TODO display assignments on the class card
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

                    setOnClickListener {
                        val intent = Intent(activity, ClassDetailActivity::class.java)
                        intent.putExtra(ItemDetailActivity.EXTRA_ITEM, cls)
                        startActivity(intent)
                    }
                }

                container.addView(card)
            }
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
                        val intent = Intent(activity, AssignmentDetailActivity::class.java).putExtra(
                                ItemDetailActivity.EXTRA_ITEM, overdueAssignments[0])
                        startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL)
                    } else {
                        val intent = Intent(activity, AssignmentsActivity::class.java).putExtra(
                                AssignmentsActivity.EXTRA_MODE, AssignmentsActivity.DISPLAY_TODO)
                        startActivity(intent)
                    }
                }
            }

            container.addView(card)
        }

        private fun addAssignmentCards(container: ViewGroup, inflater: LayoutInflater,
                                    assignments: ArrayList<Assignment>) {
            for (assignment in assignments.sorted()) {
                val card = inflater.inflate(R.layout.item_home_card_no_date, container, false)

                val cls = Class.create(context, assignment.classId)!!
                val subject = Subject.create(context, cls.subjectId)!!
                val color = Color(subject.colorId)

                with(card) {
                    findViewById(R.id.color).setBackgroundColor(
                            ContextCompat.getColor(context, color.getPrimaryColorResId(context)))

                    (findViewById(R.id.title) as TextView).text = assignment.title
                    (findViewById(R.id.subtitle) as TextView).text = cls.makeName(subject)

                    setOnClickListener {
                        val intent = Intent(activity, AssignmentDetailActivity::class.java)
                        intent.putExtra(ItemDetailActivity.EXTRA_ITEM, assignment)
                        startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL)
                    }
                }

                container.addView(card)
            }
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

            if (requestCode == REQUEST_CODE_ITEM_DETAIL) {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(LOG_TAG, "TodayFragment: received activity result - refreshing lists")
                    mSectionContainer!!.removeAllViews()
                    setupLayout()
                }
            }
        }
    }

    class UpcomingFragment : Fragment() {

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

            val assignmentSection = SectionUi.Builder(context, mSectionContainer!!)
                    .setTitle(R.string.title_activity_assignments)
                    .build()
            addAssignmentCards(assignmentSection.containerView, inflater, getUpcomingAssignments())
            mSectionContainer!!.addView(assignmentSection.view)

            val exams = getUpcomingExams()
            if (exams.isNotEmpty()) {
                val examsSection = SectionUi.Builder(context, mSectionContainer!!)
                        .setTitle(R.string.title_activity_exams)
                        .build()
                addExamCards(examsSection.containerView, inflater, exams)

                mSectionContainer!!.addView(examsSection.view)
            }
        }

        /**
         * @return a list of assignments due between today's date and next week.
         */
        private fun getUpcomingAssignments(): ArrayList<Assignment> {
            val upcomingAssignments = ArrayList<Assignment>()
            val now = LocalDate.now()
            val upperDate = now.plusWeeks(1)

            AssignmentHandler(context).getItems(activity.application).forEach {
                if (it.dueDate.isAfter(now) && it.dueDate.isBefore(upperDate)) {
                    upcomingAssignments.add(it)
                }
            }

            return upcomingAssignments
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
                        startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL)
                    }
                }

                container.addView(card)
            }
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

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == REQUEST_CODE_ITEM_DETAIL) {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(LOG_TAG, "UpcomingFragment: received activity result - refreshing lists")
                    mSectionContainer!!.removeAllViews()
                    setupLayout()
                }
            }
        }
    }

}
