/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.model.*
import co.timetableapp.ui.agenda.AgendaActivity
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.classes.ClassDetailActivity
import co.timetableapp.ui.components.SectionGroup
import co.timetableapp.ui.events.EventDetailActivity
import co.timetableapp.ui.exams.ExamDetailActivity
import co.timetableapp.util.ScheduleUtils
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

    private lateinit var mDataHelper: HomeDataHelper

    private lateinit var mSectionContainer: LinearLayout

    private val mAssignmentsToday by lazy { mDataHelper.getAssignmentsToday() }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_home_main, container, false)

        mDataHelper = HomeDataHelper(activity)

        mSectionContainer = rootView.findViewById(R.id.section_container) as LinearLayout
        setupLayout()

        return rootView
    }

    private fun setupLayout() {
        val inflater = LayoutInflater.from(context)

        setupClassSection(inflater)
        setupExamSection(inflater)
        setupEventSection(inflater)
        setupAssignmentSection(inflater)
    }

    private fun setupClassSection(inflater: LayoutInflater) {
        val classesSection = SectionGroup.Builder(context, mSectionContainer)
                .setTitle(R.string.title_activity_classes)
                .build()
        val classesToday = ScheduleUtils.getClassTimesForDate(
                activity,
                activity.application,
                LocalDate.now())
        addClassesCards(classesSection.containerView, inflater, classesToday)

        mSectionContainer.addView(classesSection.view)
    }

    private fun addClassesCards(container: ViewGroup, inflater: LayoutInflater,
                                classTimes: ArrayList<ClassTime>) {
        if (classTimes.isEmpty()) {
            val card = inflater.inflate(R.layout.item_empty_placeholder, container, false)
            (card.findViewById(R.id.placeholder_text) as TextView).setText(R.string.no_classes_today)
            container.addView(card)
            return
        }

        for ((_, _, classDetailId, _, _, startTime, endTime) in classTimes.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            // Not catching DataNotFoundException since this would have been checked when getting
            // the list of classes in getClassesToday()
            val classDetail = ClassDetail.create(context, classDetailId)
            val cls = Class.create(context, classDetail.classId)

            val subject = try {
                Subject.create(context, cls.subjectId)
            } catch (e: DataNotFoundException) {
                e.printStackTrace()
                continue
            }

            val color = Color(subject.colorId)

            val classTimesText = "$startTime\n$endTime"

            val classDetailBuilder = StringBuilder()
            classDetail.formatLocationName()?.let {
                classDetailBuilder.append(it)
            }
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

                setupAssignmentText(this, cls)

                setOnClickListener {
                    val intent = Intent(activity, ClassDetailActivity::class.java)
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM, cls)
                    startActivity(intent)
                }
            }

            container.addView(card)
        }
    }

    private fun setupAssignmentText(card: View, cls: Class) {
        val assignmentText = card.findViewById(R.id.assignment_text) as TextView
        val classAssignments = getAssignmentsForClass(mAssignmentsToday, cls)

        /*
         * To make filtering the list faster for next time, we'll remove the assignments for this
         * class from the list.
         *
         * This is also helpful as we want to display other assignments due today on separate cards
         * (i.e. assignments not related to a class today).
         */
        (mAssignmentsToday as ArrayList).removeAll(classAssignments)

        val numberDue = classAssignments.size

        if (numberDue == 0) {
            assignmentText.visibility = View.GONE
        } else {
            assignmentText.visibility = View.VISIBLE
            assignmentText.text = resources.getQuantityString(
                    R.plurals.class_card_assignment_text,
                    numberDue,
                    numberDue)
        }
    }

    private fun getAssignmentsForClass(assignments: List<Assignment>, cls: Class): List<Assignment> {
        val classAssignments = ArrayList<Assignment>()
        assignments.filterTo(classAssignments) { it.classId == cls.id }
        return classAssignments
    }

    private fun setupExamSection(inflater: LayoutInflater) {
        val exams = mDataHelper.getExamsToday()
        if (exams.isNotEmpty()) {
            val examsSection = SectionGroup.Builder(context, mSectionContainer)
                    .setTitle(R.string.title_exams)
                    .build()
            addExamsCards(examsSection.containerView, inflater, exams)

            mSectionContainer.addView(examsSection.view)
        }
    }

    private fun addExamsCards(container: ViewGroup, inflater: LayoutInflater, exams: List<Exam>) {
        for (exam in exams.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val subject = try {
                Subject.create(context, exam.subjectId)
            } catch (e: DataNotFoundException) {
                // Don't display this item but print the stack trace
                e.printStackTrace()
                continue
            }

            val color = Color(subject.colorId)

            val endTime = exam.startTime.plusMinutes(exam.duration.toLong())
            val timesText = "${exam.startTime}\n$endTime"

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

    private fun setupAssignmentSection(inflater: LayoutInflater) {
        val overdueAssignments = mDataHelper.getOverdueAssignments()
        if (mAssignmentsToday.isNotEmpty() || overdueAssignments.isNotEmpty()) {
            val otherSection = SectionGroup.Builder(context, mSectionContainer)
                    .setTitle(R.string.title_other_notices)
                    .build()

            if (mAssignmentsToday.isNotEmpty()) {
                // After removing assignments due for all classes today, there may be additional
                // assignments not for a class today, but nonetheless due today. These will be
                // displayed here.
                addAssignmentCards(otherSection.containerView, inflater, mAssignmentsToday)
            }

            if (overdueAssignments.isNotEmpty()) {
                addOverdueAssignmentsCard(otherSection.containerView, inflater, overdueAssignments)
            }

            mSectionContainer.addView(otherSection.view)
        }
    }

    private fun addOverdueAssignmentsCard(container: ViewGroup, inflater: LayoutInflater,
                                          overdueAssignments: List<Assignment>) {
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

    private fun addAssignmentCards(container: ViewGroup, inflater: LayoutInflater,
                                   assignments: List<Assignment>) {
        for (assignment in assignments.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card_no_date, container, false)

            val cls = try {
                Class.create(context, assignment.classId)
            } catch (e: DataNotFoundException) {
                // Don't show this item in the database
                e.printStackTrace()
                continue
            }

            val subject = try {
                Subject.create(context, cls.subjectId)
            } catch (e: DataNotFoundException) {
                // Don't show this item in the database
                e.printStackTrace()
                continue
            }

            val color = Color(subject.colorId)

            with(card) {
                findViewById(R.id.color).setBackgroundColor(
                        ContextCompat.getColor(context, color.getPrimaryColorResId(context)))

                (findViewById(R.id.title) as TextView).text = assignment.title
                (findViewById(R.id.subtitle) as TextView).text = cls.makeName(subject)

                setOnClickListener {
                    val intent = Intent(activity, AssignmentDetailActivity::class.java)
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM, assignment)
                    startActivityForResult(intent, MainActivity.REQUEST_CODE_ITEM_DETAIL)
                }
            }

            container.addView(card)
        }
    }

    private fun setupEventSection(inflater: LayoutInflater) {
        val events = mDataHelper.getEventsToday()
        if (events.isNotEmpty()) {
            val eventsSection = SectionGroup.Builder(context, mSectionContainer)
                    .setTitle(R.string.title_events)
                    .build()
            addEventsCards(eventsSection.containerView, inflater, events)
            mSectionContainer.addView(eventsSection.view)
        }
    }

    private fun addEventsCards(container: ViewGroup, inflater: LayoutInflater,
                               events: List<Event>) {
        for (event in events.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val timesText = "${event.startDateTime.toLocalTime()}\n${event.endDateTime.toLocalTime()}"

            with(card) {
                findViewById(R.id.color).setBackgroundColor(ContextCompat.getColor(
                        context,
                        Event.DEFAULT_COLOR.getPrimaryColorResId(context)))

                (findViewById(R.id.title) as TextView).text = event.title

                (findViewById(R.id.times) as TextView).text = timesText

                setOnClickListener {
                    val intent = Intent(activity, EventDetailActivity::class.java)
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM, event)
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
                mSectionContainer.removeAllViews()
                setupLayout()
            }
        }
    }

}
