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

package co.timetableapp.ui.classes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.model.ClassDetail
import co.timetableapp.model.ClassTime
import co.timetableapp.ui.components.TimeSelectorHelper
import co.timetableapp.util.PrefUtils
import co.timetableapp.util.isEmpty
import co.timetableapp.util.title
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import java.util.*

/**
 * This activity is started and displayed to allow the user to change a [class time][ClassTime].
 *
 * It should only be started by [ClassEditActivity] after the user has clicked on a class time. It
 * can also be started if the user wants to create a new class time. After the user has finished,
 * the result from this activity (whether there were changes made or not) would be passed back to
 * [ClassEditActivity], where the changes are displayed.
 *
 * @see ClassEditActivity
 * @see ClassTime
 */
class ClassTimeEditActivity : AppCompatActivity() {

    companion object {

        /**
         * The key for the [ClassTime] passed through an intent extra.
         *
         * It should be null if we're adding a new class time.
         */
        const val EXTRA_CLASS_TIME = "extra_class_time"

        /**
         * The key for the integer identifier of the [ClassDetail] passed through an intent extra.
         *
         * This is used in [handleDoneAction] when creating [class details][ClassDetail] and adding
         * the new data to the database.
         *
         * @see ClassTime.classDetailId
         * @see ClassDetail.id
         */
        const val EXTRA_CLASS_DETAIL_ID = "extra_class_detail_id"

        /**
         * The key passed through an intent extra for the index of the tab where this class time is
         * displayed in [ClassEditActivity].
         *
         * It is used in [handleDoneAction] and [handleCloseAction] to pass back this value to
         * [ClassEditActivity] so that only the class time values for the relevant tab can be
         * updated.
         */
        const val EXTRA_TAB_POSITION = "extra_tab_position"
    }

    private var mClassDetailId = 0
    private var mTabPos = 0

    private var mClassTimes: ArrayList<ClassTime>? = null
    private var mIsNewTime = false

    private val mClassTimeHandler = ClassTimeHandler(this)

    private lateinit var mStartTime: LocalTime
    private lateinit var mStartTimeHelper: TimeSelectorHelper

    private lateinit var mEndTime: LocalTime
    private lateinit var mEndTimeHelper: TimeSelectorHelper

    private lateinit var mDayText: TextView
    private lateinit var mDayDialog: AlertDialog
    private val mDaysOfWeek = SparseArray<DayOfWeek>()

    private lateinit var mWeekText: TextView
    private lateinit var mWeekDialog: AlertDialog
    private val mWeekNumbers = SparseArray<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_time_edit)

        val extras = intent.extras
        mClassDetailId = extras.getInt(EXTRA_CLASS_DETAIL_ID)
        mTabPos = extras.getInt(EXTRA_TAB_POSITION)

        mClassTimes = extras.getParcelableArrayList<ClassTime>(EXTRA_CLASS_TIME)
        mIsNewTime = mClassTimes == null

        validateClassTimes()

        setupLayout()
    }

    /**
     * Ensures that all class times have the same start and end *dates*.
     *
     * This is necessary because of how class times are presented and grouped in this page.
     * TODO: documentation here is unclear
     */
    private fun validateClassTimes() {
        if (mIsNewTime) {
            // We won't have any class times to validate if it's new
            return
        }

        var baseStartTime: LocalTime? = null
        var baseEndTime: LocalTime? = null

        for ((_, _, _, _, _, startTime, endTime) in mClassTimes!!) {
            if (baseStartTime == null) {
                // We need values to compare/validate against, so set these first
                baseStartTime = startTime
                baseEndTime = endTime
                continue
            }

            if (startTime != baseStartTime || endTime != baseEndTime) {
                throw IllegalArgumentException("invalid time - all start and end times must be " +
                        "the same")
            }
        }
    }

    private fun setupLayout() {
        setupToolbar()
        setupDayText()
        setupWeekText()
        setupTimeTexts()
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val titleResId = if (mIsNewTime) {
            R.string.title_activity_class_time_new
        } else {
            R.string.title_activity_class_time_edit
        }

        supportActionBar!!.title = getString(titleResId)

        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { handleCloseAction() }
    }

    private fun setupDayText() {
        mDayText = findViewById(R.id.textView_day) as TextView

        if (!mIsNewTime) {
            for ((_, _, _, dayOfWeek) in mClassTimes!!) {
                mDaysOfWeek.put(dayOfWeek.value - 1, dayOfWeek)
            }
            updateDayText()
        }

        mDayText.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            val checkedItems = BooleanArray(7)
            for (i in 0..6) {
                checkedItems[i] = mDaysOfWeek[i] != null
            }

            builder.setTitle(R.string.property_days)
                    .setMultiChoiceItems(R.array.days, checkedItems) { _, which, isChecked ->
                        if (isChecked) {
                            mDaysOfWeek.put(which, DayOfWeek.of(which + 1))
                        } else {
                            mDaysOfWeek.remove(which)
                        }
                    }
                    .setPositiveButton(R.string.action_done) { _, _ ->
                        updateDayText()
                        mDayDialog.dismiss()
                    }

            mDayDialog = builder.create()
            mDayDialog.show()
        }
    }

    private fun updateDayText() {
        if (mDaysOfWeek.isEmpty()) {
            mDayText.setText(R.string.property_days)
            mDayText.setTextColor(ContextCompat.getColor(this, R.color.mdu_text_black_secondary))
            return
        }

        val builder = StringBuilder()
        for (i in 0..6) {
            val dayOfWeek = mDaysOfWeek[i]
            if (mDaysOfWeek[i] != null) {
                builder.append(dayOfWeek.toString().toLowerCase().title())
                        .append(", ")
            }
        }
        val text = builder.toString()
        val displayed = text.substring(0, text.length - 2)

        mDayText.text = displayed
        mDayText.setTextColor(ContextCompat.getColor(this, R.color.mdu_text_black))
    }

    private fun setupWeekText() {
        val timetable = (application as TimetableApplication).currentTimetable!!
        val weekRotations = timetable.weekRotations

        mWeekText = findViewById(R.id.textView_week) as TextView

        if (timetable.hasFixedScheduling()) {
            mWeekText.visibility = View.GONE
            findViewById(R.id.divider).visibility = View.GONE
            mWeekNumbers.put(0, 1)

        } else {
            if (!mIsNewTime) {
                for ((_, _, _, _, weekNumber) in mClassTimes!!) {
                    mWeekNumbers.put(weekNumber - 1, weekNumber)
                }
                updateWeekText()
            }

            mWeekText.setOnClickListener {
                val builder = AlertDialog.Builder(this)

                val weekItemsList = ArrayList<String>()
                for (i in 1..weekRotations) {
                    val item = ClassTime.getWeekText(this, i)
                    weekItemsList.add(item)
                }
                val weekItems = weekItemsList.toTypedArray()

                val checkedItems = BooleanArray(weekRotations)
                for (i in 0..weekRotations - 1) {
                    checkedItems[i] = mWeekNumbers[i] != null
                }

                builder.setTitle(R.string.property_weeks)
                        .setMultiChoiceItems(weekItems, checkedItems) { _, which, isChecked ->
                            if (isChecked) {
                                mWeekNumbers.put(which, which + 1)
                            } else {
                                mWeekNumbers.remove(which)
                            }
                        }
                        .setPositiveButton(R.string.action_done) { _, _ ->
                            updateWeekText()
                            mWeekDialog.dismiss()
                        }

                mWeekDialog = builder.create()
                mWeekDialog.show()
            }
        }
    }

    private fun updateWeekText() {
        if (mWeekNumbers.isEmpty()) {
            mWeekText.setText(R.string.property_weeks)
            mWeekText.setTextColor(ContextCompat.getColor(this, R.color.mdu_text_black_secondary))
            return
        }

        val weekRotations = (application as TimetableApplication).currentTimetable!!.weekRotations

        val builder = StringBuilder()
        for (i in 0..weekRotations - 1) {
            if (mWeekNumbers[i] != null) {
                val weekNumber = mWeekNumbers[i]
                builder.append(ClassTime.getWeekText(this, weekNumber))
                        .append(", ")
            }
        }
        val text = builder.toString()
        val displayed = text.substring(0, text.length - 2)

        mWeekText.text = displayed
        mWeekText.setTextColor(ContextCompat.getColor(this, R.color.mdu_text_black))
    }

    private fun setupTimeTexts() {
        if (!mIsNewTime) {
            mStartTime = mClassTimes!![0].startTime
            mEndTime = mClassTimes!![0].endTime
        } else {
            val defaultDuration = PrefUtils.getDefaultLessonDuration(this).toLong()
            mStartTime = LocalTime.of(9, 0)
            mEndTime = mStartTime.plusMinutes(defaultDuration)
        }

        mStartTimeHelper = TimeSelectorHelper(this, R.id.textView_start_time)
        mStartTimeHelper.setup(mStartTime) { _, time ->
            mStartTime = time
            mStartTimeHelper.updateTime(mStartTime)
        }

        mEndTimeHelper = TimeSelectorHelper(this, R.id.textView_end_time)
        mEndTimeHelper.setup(mEndTime) { _, time ->
            mEndTime = time
            mEndTimeHelper.updateTime(mEndTime)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_item_edit, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        if (mIsNewTime) {
            menu.findItem(R.id.action_delete).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_done -> handleDoneAction()
            R.id.action_delete -> handleDeleteAction()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = handleCloseAction()

    private fun handleCloseAction() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    private fun handleDoneAction() {
        if (mDaysOfWeek.isEmpty()) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_days_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if (mWeekNumbers.isEmpty()) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_weeks_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if (mStartTime == mEndTime) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_start_time_equal_end,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if (mStartTime.isAfter(mEndTime)) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_start_time_after_end,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val timetable = (application as TimetableApplication).currentTimetable!!

        if (!mIsNewTime) {
            for ((id) in mClassTimes!!) {
                mClassTimeHandler.deleteItemWithReferences(id)
            }
        }

        for (i in 0..timetable.weekRotations - 1) {
            if (mWeekNumbers[i] == null) {
                continue
            }
            val weekNumber = mWeekNumbers[i]

            for (j in 0..6) {
                val dayOfWeek = mDaysOfWeek[j] ?: continue

                val id = mClassTimeHandler.getHighestItemId() + 1

                val classTime = ClassTime(
                        id,
                        timetable.id,
                        mClassDetailId,
                        dayOfWeek,
                        weekNumber,
                        mStartTime,
                        mEndTime
                )

                // Everything will be added fresh regardless of whether or not it is new.
                // This is because there may be more or less ClassTimes than before so ids cannot
                // be replaced exactly (delete 1, add 1).
                mClassTimeHandler.addItem(classTime)
                ClassTimeHandler.addAlarmsForClassTime(this, classTime)
            }

            val intent = Intent().putExtra(EXTRA_TAB_POSITION, mTabPos)
            setResult(Activity.RESULT_OK, intent)
            supportFinishAfterTransition()
        }
    }

    private fun handleDeleteAction() {
        for ((id) in mClassTimes!!) {
            mClassTimeHandler.deleteItemWithReferences(id)
        }

        val intent = Intent().putExtra(EXTRA_TAB_POSITION, mTabPos)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
