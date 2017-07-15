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
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.components.SectionGroup
import co.timetableapp.ui.events.EventDetailActivity
import co.timetableapp.ui.exams.ExamDetailActivity
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

        /**
         * Upcoming items occurring between today and this number of days after today will be shown.
         */
        private const val MAX_DAYS_UPCOMING = 7L
    }

    private lateinit var mSectionContainer: LinearLayout

    private lateinit var mDataHelper: HomeDataHelper

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

        setupExamSection(inflater)
        setupAssignmentSection(inflater)
        setupEventSection(inflater)
    }

    private fun setupExamSection(inflater: LayoutInflater) {
        val exams = mDataHelper.getUpcomingExams(MAX_DAYS_UPCOMING)
        if (exams.isNotEmpty()) {
            val examsSection = SectionGroup.Builder(context, mSectionContainer)
                    .setTitle(R.string.title_exams)
                    .build()
            addExamCards(examsSection.containerView, inflater, exams)

            mSectionContainer.addView(examsSection.view)
        }
    }

    private fun addExamCards(container: ViewGroup, inflater: LayoutInflater, exams: List<Exam>) {
        if (exams.isEmpty()) {
            val card = inflater.inflate(R.layout.item_empty_placeholder, container, false)
            container.addView(card)
            return
        }

        for (exam in exams.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val subject = try {
                Subject.create(context, exam.subjectId)
            } catch (e: DataNotFoundException) {
                e.printStackTrace()
                continue
            }

            val color = Color(subject.colorId)

            val formatter = DateTimeFormatter.ofPattern("EEE\nHH:mm")
            val datesText = exam.getDateTime().format(formatter).toUpperCase()

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
        val assignmentSection = SectionGroup.Builder(context, mSectionContainer)
                .setTitle(R.string.title_assignments)
                .build()
        addAssignmentCards(
                assignmentSection.containerView,
                inflater,
                mDataHelper.getUpcomingAssignments(MAX_DAYS_UPCOMING))
        mSectionContainer.addView(assignmentSection.view)
    }

    private fun addAssignmentCards(container: ViewGroup, inflater: LayoutInflater,
                                   assignments: List<Assignment>) {
        if (assignments.isEmpty()) {
            val card = inflater.inflate(R.layout.item_empty_placeholder, container, false)
            container.addView(card)
            return
        }

        for (assignment in assignments.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            // Not checking for DataNotFoundException since this would have been handled when
            // getting the list of assignments
            val cls = Class.create(context, assignment.classId)

            val subject = try {
                Subject.create(context, cls.subjectId)
            } catch (e: DataNotFoundException) {
                e.printStackTrace()
                continue
            }

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
        val events = mDataHelper.getUpcomingEvents(MAX_DAYS_UPCOMING)
        if (events.isEmpty()) {
            return
        }

        val assignmentSection = SectionGroup.Builder(context, mSectionContainer)
                .setTitle(R.string.title_events)
                .build()
        addEventCards(assignmentSection.containerView, inflater, events)
        mSectionContainer.addView(assignmentSection.view)
    }

    private fun addEventCards(container: ViewGroup, inflater: LayoutInflater, events: List<Event>) {
        for (event in events.sorted()) {
            val card = inflater.inflate(R.layout.item_home_card, container, false)

            val formatter = DateTimeFormatter.ofPattern("EEE\nHH:mm")
            val datesText = event.startDateTime.format(formatter).toUpperCase()

            with(card) {
                findViewById(R.id.color).setBackgroundColor(ContextCompat.getColor(
                        context,
                        Event.DEFAULT_COLOR.getPrimaryColorResId(context)))

                (findViewById(R.id.title) as TextView).text = event.title
                (findViewById(R.id.times) as TextView).text = datesText

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
                Log.d(LOG_TAG, "UpcomingFragment: received activity result - refreshing lists")
                mSectionContainer.removeAllViews()
                setupLayout()
            }
        }
    }

}
