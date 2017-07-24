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
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.timetableapp.R
import co.timetableapp.model.*
import co.timetableapp.model.home.HomeHeader
import co.timetableapp.model.home.HomeItem
import co.timetableapp.model.home.HomeListItem
import co.timetableapp.ui.agenda.AgendaActivity
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.classes.ClassDetailActivity
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

    private lateinit var mAdapter: HomeItemsAdapter

    private val mItems = ArrayList<HomeListItem>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_home_main, container, false)
        setupLayout(rootView)
        return rootView
    }

    private fun setupLayout(rootView: View) {
        populateList()

        Log.d(LOG_TAG, mItems.toString())

        mAdapter = HomeItemsAdapter(activity, mItems)
        mAdapter.onHeaderClick { view, position ->
            val header = mItems[position] as HomeHeader
            header.onClick?.invoke(view, position)
        }
        mAdapter.onItemClick { _, position ->
            val item = mItems[position] as HomeItem
            val intent = when (item) {
                is ClassTime -> {
                    val cls = Class.create(
                            activity,
                            ClassDetail.create(activity, item.classDetailId).classId
                    )
                    Intent(activity, ClassDetailActivity::class.java)
                            .putExtra(ItemDetailActivity.EXTRA_ITEM, cls)
                }
                is Exam -> {
                    Intent(activity, ExamDetailActivity::class.java)
                            .putExtra(ItemDetailActivity.EXTRA_ITEM, item)
                }
                is Event -> {
                    Intent(activity, EventDetailActivity::class.java)
                            .putExtra(ItemDetailActivity.EXTRA_ITEM, item)
                }
                is Assignment -> {
                    Intent(activity, AssignmentDetailActivity::class.java)
                            .putExtra(ItemDetailActivity.EXTRA_ITEM, item)
                }
                is OverdueAssignmentsItem -> {
                    Intent(activity, AgendaActivity::class.java)
                }
                else -> throw IllegalArgumentException("invalid item type: $item")
            }
            startActivity(intent)
        }

        val recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerView)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
            adapter = mAdapter
        }
    }

    /**
     * Adds the items being displayed to the list.
     */
    private fun populateList() {
        val processor = DataProcessor(activity)

        with(mItems) {
            addAll(processor.getClassListItems())
            addAll(processor.getOtherAssignmentListItems())
            addAll(processor.getOverdueAssignmentItems())
            addAll(processor.getExamListItems())
            addAll(processor.getEventListItems())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.REQUEST_CODE_ITEM_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(LOG_TAG, "TodayFragment: received activity result - refreshing lists")
                mItems.clear()
                populateList()
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * A helper class for managing and organizing data displayed on this part of the home page.
     */
    private class DataProcessor(private val activity: Activity) {

        private val mDataHelper = HomeDataHelper(activity)

        /**
         * This field stores the list of assignments due today. Assignments due for particular
         * classes today are removed, so that after [getClassListItems] we only have a list of
         * assignments due today not belonging to any classes today.
         *
         * @see removeClassAssignments
         */
        private val mAssignmentsToday by lazy { mDataHelper.getAssignmentsToday() }

        fun getClassListItems() = getDisplayedItems(
                ScheduleUtils.getClassTimesForDate(activity, activity.application, LocalDate.now()),
                R.string.title_activity_classes,
                { removeClassAssignments(it as ClassTime) }
        )

        /**
         * Removes the assignments due for a particular [classTime] from [mAssignmentsToday].
         */
        private fun removeClassAssignments(classTime: ClassTime) {
            val classDetail = ClassDetail.create(activity, classTime.classDetailId)
            val cls = Class.create(activity, classDetail.classId)

            (mAssignmentsToday as ArrayList).removeAll(
                    mDataHelper.getAssignmentsToday().filter { it.classId == cls.id }
            )
        }

        fun getOtherAssignmentListItems() = getDisplayedItems(
                mAssignmentsToday,
                R.string.title_other_notices
        )

        fun getOverdueAssignmentItems() = if (mDataHelper.getOverdueAssignments().isEmpty()) {
            emptyList()
        } else {
            getDisplayedItems(
                    listOf(OverdueAssignmentsItem(mDataHelper.getOverdueAssignments())),
                    R.string.title_other_notices)
        }

        fun getExamListItems() =
                getDisplayedItems(mDataHelper.getExamsToday(), R.string.title_exams)

        fun getEventListItems() =
                getDisplayedItems(mDataHelper.getEventsToday(), R.string.title_events)

        /**
         * A helper function to return a list of items to display, including headers.
         *
         * @param items     the list of a type of [HomeItem]
         * @param titleRes  the string resource for the title of the header to use for these [items]
         * @param action    an action to perform on each item before a header is added. This is null
         *                  by default, in which case no action will be performed.
         *
         * @return  the list of items to display. This can be an empty list if there are no [items]
         *          and thus no headers are needed.
         */
        private fun getDisplayedItems(items: List<HomeItem>,
                                      @StringRes titleRes: Int,
                                      action: ((HomeItem) -> Unit)? = null): List<HomeListItem> {
            if (items.isEmpty()) return emptyList()

            action?.let { for (item in items) action(item) }

            val list = ArrayList<HomeListItem>()
            list.add(HomeHeader(activity.getString(titleRes)))
            list.addAll(items)
            return list
        }

    }

}
