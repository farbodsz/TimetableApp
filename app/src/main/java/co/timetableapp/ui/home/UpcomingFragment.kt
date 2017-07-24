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
import co.timetableapp.model.Assignment
import co.timetableapp.model.Event
import co.timetableapp.model.Exam
import co.timetableapp.model.home.HomeHeader
import co.timetableapp.model.home.HomeItem
import co.timetableapp.model.home.HomeListItem
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.events.EventDetailActivity
import co.timetableapp.ui.exams.ExamDetailActivity
import co.timetableapp.ui.home.UpcomingFragment.Companion.MAX_DAYS_UPCOMING

/**
 * This page displays the user's upcoming classes and exams.
 *
 * 'Upcoming' is defined by [MAX_DAYS_UPCOMING].
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

        mAdapter = HomeItemsAdapter(activity, mItems)
        mAdapter.onHeaderClick { view, position ->
            val header = mItems[position] as HomeHeader
            header.onClick?.invoke(view, position)
        }
        mAdapter.onItemClick { _, position ->
            val item = mItems[position] as HomeItem
            val intent = when (item) {
                is Assignment -> {
                    Intent(activity, AssignmentDetailActivity::class.java)
                            .putExtra(ItemDetailActivity.EXTRA_ITEM, item)
                }
                is Exam -> {
                    Intent(activity, ExamDetailActivity::class.java)
                            .putExtra(ItemDetailActivity.EXTRA_ITEM, item)
                }
                is Event -> {
                    Intent(activity, EventDetailActivity::class.java)
                            .putExtra(ItemDetailActivity.EXTRA_ITEM, item)
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
            addAll(processor.getAssignmentItems())
            addAll(processor.getExamItems())
            addAll(processor.getEventItems())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.REQUEST_CODE_ITEM_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(LOG_TAG, "UpcomingFragment: received activity result - refreshing lists")
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

        fun getAssignmentItems() = getDisplayedItems(
                mDataHelper.getUpcomingAssignments(MAX_DAYS_UPCOMING),
                R.string.title_assignments
        )

        fun getExamItems() = getDisplayedItems(
                mDataHelper.getUpcomingExams(MAX_DAYS_UPCOMING),
                R.string.title_exams
        )

        fun getEventItems() = getDisplayedItems(
                mDataHelper.getUpcomingEvents(MAX_DAYS_UPCOMING),
                R.string.title_events
        )

        private fun getDisplayedItems(items: List<HomeListItem>,
                                      @StringRes titleRes: Int): List<HomeListItem> {
            if (items.isEmpty()) return emptyList()

            val list = ArrayList<HomeListItem>()
            list.add(HomeHeader(activity.getString(titleRes)))
            list.addAll(items)
            return list
        }

    }

}
