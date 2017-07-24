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

package co.timetableapp.ui.timetables

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.data.handler.TermHandler
import co.timetableapp.data.handler.TimetableHandler
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.ClassTimesSchema
import co.timetableapp.data.schema.TermsSchema
import co.timetableapp.model.ClassTime
import co.timetableapp.model.Term
import co.timetableapp.model.Timetable
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import co.timetableapp.util.title
import com.satsuware.usefulviews.LabelledSpinner
import org.threeten.bp.LocalDate

/**
 * An activity for the user to edit a [Timetable].
 *
 * @see TimetablesActivity
 */
class TimetableEditActivity : ItemEditActivity<Timetable>(), LabelledSpinner.OnItemChosenListener {

    companion object {
        private const val REQUEST_CODE_TERM_EDIT = 1
    }

    private var mIsFirst = false

    private val mTimetableHandler = TimetableHandler(this)

    private lateinit var mNameEditText: EditText

    private var mStartDate: LocalDate? = null
    private var mEndDate: LocalDate? = null
    private lateinit var mStartDateText: TextView
    private lateinit var mEndDateText: TextView

    private var mWeekRotations = 0
    private lateinit var mSchedulingSpinner: LabelledSpinner
    private lateinit var mWeekRotationSpinner: LabelledSpinner

    private lateinit var mTerms: ArrayList<Term>
    private lateinit var mAdapter: TermsAdapter

    override fun getLayoutResource() = R.layout.activity_timetable_edit

    override fun handleExtras() {
        super.handleExtras()
        mIsFirst = (application as TimetableApplication).currentTimetable == null
    }

    override fun getTitleRes(isNewItem: Boolean) = if (isNewItem) {
        R.string.title_activity_timetable_new
    } else {
        R.string.title_activity_timetable_edit
    }

    override fun setupLayout() {
        mNameEditText = findViewById(R.id.editText_name)
        if (!mIsNew) {
            mNameEditText.setText(mItem!!.name)
        }

        setupDateTexts()

        mSchedulingSpinner = findViewById(R.id.spinner_scheduling_type)
        mWeekRotationSpinner = findViewById(R.id.spinner_scheduling_detail)

        mSchedulingSpinner.onItemChosenListener = this
        mWeekRotationSpinner.onItemChosenListener = this

        mWeekRotations = if (mIsNew) 1 else mItem!!.weekRotations
        updateSchedulingSpinners()

        setupTermsList()

        setupAddTermButton()
    }

    private fun setupDateTexts() {
        mStartDateText = findViewById(R.id.textView_start_date)
        mEndDateText = findViewById(R.id.textView_end_date)

        if (!mIsNew) {
            mStartDate = mItem!!.startDate
            mEndDate = mItem!!.endDate
            updateDateTexts()
        }

        // Note: -1 and +1s in code because Android month values are from 0-11 (to correspond with
        // java.util.Calendar) but LocalDate month values are from 1-12.

        mStartDateText.setOnClickListener {
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mStartDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateDateTexts()
            }

            DatePickerDialog(
                    this,
                    listener,
                    if (mIsNew) LocalDate.now().year else mStartDate!!.year,
                    if (mIsNew) LocalDate.now().monthValue - 1 else mStartDate!!.monthValue - 1,
                    if (mIsNew) LocalDate.now().dayOfMonth else mStartDate!!.dayOfMonth
            ).show()
        }

        mEndDateText.setOnClickListener {
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mEndDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateDateTexts()
            }

            DatePickerDialog(
                    this,
                    listener,
                    if (mIsNew) LocalDate.now().year else mEndDate!!.year,
                    if (mIsNew) LocalDate.now().monthValue - 1 else mEndDate!!.monthValue - 1,
                    if (mIsNew) LocalDate.now().dayOfMonth else mEndDate!!.dayOfMonth
            ).show()
        }
    }

    private fun updateDateTexts() {
        mStartDate?.let {
            mStartDateText.text = it.format(DateUtils.FORMATTER_FULL_DATE)
            mStartDateText.setTextColor(ContextCompat.getColor(baseContext, R.color.mdu_text_black))
        }

        mEndDate?.let {
            mEndDateText.text = it.format(DateUtils.FORMATTER_FULL_DATE)
            mEndDateText.setTextColor(ContextCompat.getColor(baseContext, R.color.mdu_text_black))
        }
    }

    private fun updateSchedulingSpinners() {
        if (mWeekRotations == 1) {
            mSchedulingSpinner.setSelection(0)
            mWeekRotationSpinner.visibility = View.GONE

        } else {
            mSchedulingSpinner.setSelection(1)

            mWeekRotationSpinner.visibility = View.VISIBLE
            // e.g. weekRotations of 2 will be position 0 as in the string-array
            mWeekRotationSpinner.setSelection(mWeekRotations - 2)
        }
    }

    private fun setupTermsList() {
        mTerms = getTermsForTimetable(findTimetableId())
        mTerms.sort()

        mAdapter = TermsAdapter(mTerms)
        mAdapter.onItemClick { view, position ->
            val intent = Intent(this, TermEditActivity::class.java)
            intent.putExtra(ItemEditActivity.EXTRA_ITEM, mTerms[position])
            intent.putExtra(TermEditActivity.EXTRA_TIMETABLE_ID, findTimetableId())

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.transition_2))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_TERM_EDIT, bundle)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        with(recyclerView) {
            setHasFixedSize(true)
            layoutManager = object : LinearLayoutManager(this@TimetableEditActivity) {
                override fun canScrollVertically() = false
            }
            adapter = mAdapter
        }
    }

    private fun setupAddTermButton() {
        val btnAddTerm = findViewById<Button>(R.id.button_add_term)
        btnAddTerm.setOnClickListener { view ->
            val intent = Intent(this, TermEditActivity::class.java)
            intent.putExtra(TermEditActivity.EXTRA_TIMETABLE_ID, findTimetableId())

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.transition_2))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_TERM_EDIT, bundle)
        }
    }

    override fun onItemChosen(labelledSpinner: View?, adapterView: AdapterView<*>?, itemView: View?, position: Int, id: Long) {
        when (labelledSpinner!!.id) {
            R.id.spinner_scheduling_type -> {
                val isFixedScheduling = position == 0

                if (isFixedScheduling) {
                    mWeekRotations = 1
                } else {
                    mWeekRotations = if (mIsNew || mItem!!.weekRotations == 1)
                        2
                    else
                        mItem!!.weekRotations
                }
                updateSchedulingSpinners()
            }

            R.id.spinner_scheduling_detail -> {
                if (mWeekRotations != 1) {  // only modify mWeekRotations if not fixed scheduling
                    mWeekRotations = position + 2 // as '2 weeks' is position 0
                }
                updateSchedulingSpinners()
            }
        }
    }

    override fun onNothingChosen(labelledSpinner: View?, adapterView: AdapterView<*>?) {}

    private fun refreshList() {
        mTerms.clear()
        mTerms.addAll(getTermsForTimetable(findTimetableId()))
        mTerms.sort()
        mAdapter.notifyDataSetChanged()
    }

    private fun getTermsForTimetable(timetableId: Int): ArrayList<Term> {
        val query = Query.Builder()
                .addFilter(Filters.equal(TermsSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .build()
        return TermHandler(this).getAllItems(query)
    }

    private fun findTimetableId(): Int {
        return if (mItem == null) mTimetableHandler.getHighestItemId() + 1 else mItem!!.id
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TERM_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                refreshList()
            }
        }
    }

    override fun handleCloseAction() {
        if (mIsFirst) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_first_timetable_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    override fun handleDoneAction() {
        val name = mNameEditText.text.toString().title()

        if (mStartDate == null || mEndDate == null) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_times_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if (mStartDate == mEndDate) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_start_time_equal_end,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if (mStartDate!!.isAfter(mEndDate!!)) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_start_time_after_end,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if (!mIsNew) {
            // Delete class times with an invalid week number
            if (mWeekRotations < mItem!!.weekRotations) {
                val helper = TimetableDbHelper.getInstance(this)
                val cursor = helper.readableDatabase.query(
                        ClassTimesSchema.TABLE_NAME, null,
                        ClassTimesSchema.COL_WEEK_NUMBER + ">?",
                        arrayOf(mWeekRotations.toString()), null, null, null)
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val (id) = ClassTime.from(cursor)
                    ClassTimeHandler(this).deleteItemWithReferences(id)
                    cursor.moveToNext()
                }
                cursor.close()
            }
        }

        mItem = Timetable(findTimetableId(), name, mStartDate!!, mEndDate!!, mWeekRotations)

        if (mIsNew) {
            mTimetableHandler.addItem(mItem!!)
        } else {
            mTimetableHandler.replaceItem(mItem!!.id, mItem!!)
        }

        val application = application as TimetableApplication
        application.setCurrentTimetable(this, mItem!!)

        setResult(Activity.RESULT_OK)
        supportFinishAfterTransition()
    }

    override fun handleDeleteAction() {
        // There needs to be at least one timetable for the app to work
        if (mTimetableHandler.getAllItems().size == 1) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_first_timetable_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(this)
                .setTitle(R.string.delete_timetable)
                .setMessage(R.string.delete_confirmation_timetable)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    mTimetableHandler.deleteItemWithReferences(mItem!!.id)

                    // After the timetable has been deleted, change the current timetable
                    val newCurrentTimetable = mTimetableHandler.getAllItems()[0]

                    val timetableApp = application as TimetableApplication
                    timetableApp.setCurrentTimetable(baseContext, newCurrentTimetable)

                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

}
