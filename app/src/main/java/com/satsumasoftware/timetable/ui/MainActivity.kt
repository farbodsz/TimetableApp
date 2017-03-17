package com.satsumasoftware.timetable.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.handler.ClassTimeHandler
import com.satsumasoftware.timetable.db.handler.ExamHandler
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.db.schema.ClassTimesSchema
import com.satsumasoftware.timetable.db.schema.ExamsSchema
import com.satsumasoftware.timetable.framework.*
import com.satsumasoftware.timetable.util.DateUtils
import com.satsumasoftware.timetable.util.SectionUi
import org.threeten.bp.LocalDate

/**
 * The main screen showing an overview of the user's classes, assignments and exams.
 *
 * The UI is divided into two pages: one for today, and another for upcoming events. The user can
 * navigate between the pages using tabs.
 */
class MainActivity : NavigationDrawerActivity() {

    companion object {
        private const val LOG_TAG = "MainActivity"
    }

    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupLayout()
    }

    private fun setupLayout() {
        mViewPager = findViewById(R.id.viewPager) as ViewPager
        mViewPager!!.adapter = PagerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabLayout) as TabLayout
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white))

        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(mViewPager)
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
     * This page displays the user's classes for today.
     */
    class TodayFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_home_main, container, false)

            setupLayout(rootView)

            return rootView
        }

        private fun setupLayout(rootView: View) {
            val timetableId = (activity.application as TimetableApplication).currentTimetable!!.id

            val sectionContainer = rootView.findViewById(R.id.section_container) as LinearLayout
            val inflater = LayoutInflater.from(context)

            val classesSection = SectionUi.Builder(context, sectionContainer)
                    .setTitle(R.string.title_activity_classes)
                    .build()
            addClassesCards(classesSection.containerView, inflater, getClassesToday(timetableId))

            sectionContainer.addView(classesSection.view)

            val exams = getExamsToday(timetableId)
            if (exams.isNotEmpty()) {
                val examsSection = SectionUi.Builder(context, sectionContainer)
                        .setTitle(R.string.title_activity_exams)
                        .build()
                addExamsCards(examsSection.containerView, inflater, exams)

                sectionContainer.addView(examsSection.view)
            }

        }

        private fun addClassesCards(container: ViewGroup, inflater: LayoutInflater,
                                    classTimes: ArrayList<ClassTime>) {
            if (classTimes.isEmpty()) {
                val card = inflater.inflate(R.layout.item_empty_placeholder, container, false)
                container.addView(card)
                return
            }

            classTimes.sort()

            for (classTime in classTimes) {
                val card = inflater.inflate(R.layout.item_home_card, container, false)

                val classDetail = ClassDetail.create(context, classTime.classDetailId)
                val cls = Class.create(context, classDetail.classId)!!
                val subject = Subject.create(context, cls.subjectId)!!

                val color = Color(subject.colorId)
                card.findViewById(R.id.color).setBackgroundColor(
                        ContextCompat.getColor(context, color.getPrimaryColorResId(context)))

                val classTimesText =
                        classTime.startTime.toString() + "\n" + classTime.endTime.toString()
                (card.findViewById(R.id.times) as TextView).text = classTimesText

                (card.findViewById(R.id.title) as TextView).text = Class.makeName(cls, subject)

                val classDetailBuilder = StringBuilder()
                classDetailBuilder.append(classDetail.formatLocationName())
                if (classDetail.hasTeacher()) {
                    classDetailBuilder.append(" \u2022 ")
                            .append(classDetail.teacher)
                }
                (card.findViewById(R.id.subtitle) as TextView).text =
                        classDetailBuilder.toString()

                card.setOnClickListener {
                    // TODO ClassDetailActivity
                }

                container.addView(card)
            }
        }

        private fun addExamsCards(container: ViewGroup, inflater: LayoutInflater,
                                    exams: ArrayList<Exam>) {
            exams.sort()

            for (exam in exams) {
                val card = inflater.inflate(R.layout.item_home_card, container, false)

                val subject = Subject.create(context, exam.subjectId)!!

                val color = Color(subject.colorId)
                card.findViewById(R.id.color).setBackgroundColor(
                        ContextCompat.getColor(context, color.getPrimaryColorResId(context)))

                val endTime = exam.startTime.plusMinutes(exam.duration.toLong())

                val timesText =
                        exam.startTime.toString() + "\n" + endTime.toString()
                (card.findViewById(R.id.times) as TextView).text = timesText

                (card.findViewById(R.id.title) as TextView).text = Exam.makeName(exam, subject)

                card.setOnClickListener {
                    // TODO ClassDetailActivity
                }

                container.addView(card)
            }
        }

        private fun getClassesToday(timetableId: Int): ArrayList<ClassTime> {
            val now = LocalDate.now()
            val today = now.dayOfWeek
            val weekNumber = DateUtils.findWeekNumber(activity.application)

            val query = Query.Builder()
                    .addFilter(Filters.equal(ClassTimesSchema.COL_TIMETABLE_ID, timetableId.toString()))
                    .addFilter(Filters.equal(ClassTimesSchema.COL_DAY, today.value.toString()))
                    .addFilter(Filters.equal(ClassTimesSchema.COL_WEEK_NUMBER, weekNumber.toString()))
                    .build()

            return ClassTimeHandler(activity).getAllItems(query)
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
    }

    class UpcomingFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return super.onCreateView(inflater, container, savedInstanceState)
        }
    }

}
