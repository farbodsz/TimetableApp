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
import android.support.design.widget.Snackbar
import android.widget.EditText
import co.timetableapp.R
import co.timetableapp.data.handler.TermHandler
import co.timetableapp.data.handler.TimetableHandler
import co.timetableapp.model.Term
import co.timetableapp.model.Timetable
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.ui.components.DateSelectorHelper
import co.timetableapp.util.title
import org.threeten.bp.LocalDate

/**
 * Allows the user to edit a [Term]
 *
 * @see ItemEditActivity
 * @see TimetableEditActivity
 */
class TermEditActivity : ItemEditActivity<Term>() {

    companion object {

        /**
         * The key for the integer identifier of the [Timetable] this [Term] belongs to.
         *
         * This is passed to this activity since the timetable identifier is a required attribute of
         * a term, so if we modify of create a term, we need this attribute.
         *
         * @see Timetable.id
         */
        const val EXTRA_TIMETABLE_ID = "extra_timetable_id"
    }

    private var mTimetableId = 0

    private val mDataHandler = TermHandler(this)

    private lateinit var mEditText: EditText

    private var mStartDate: LocalDate? = null
    private lateinit var mStartDateHelper: DateSelectorHelper

    private var mEndDate: LocalDate? = null
    private lateinit var mEndDateHelper: DateSelectorHelper

    override fun getLayoutResource() = R.layout.activity_term_edit

    override fun handleExtras() {
        // We also need to get the timetable id for this term
        intent.extras?.let { extras ->
            mItem = extras.getParcelable<Term>(ItemEditActivity.EXTRA_ITEM)

            // TODO: find a better way of doing this
            // If the timetable is new, then it may not be saved to the database. In this case the
            // value passed for the id will be -1. If this is the case, we need to find what the id
            // would be.
            mTimetableId = extras.getInt(EXTRA_TIMETABLE_ID, -1)
            if (mTimetableId == -1) {
                mTimetableId = TimetableHandler(this).getHighestItemId() + 1
            }
        }

        mIsNew = mItem == null
    }

    override fun getTitleRes(isNewItem: Boolean) = if (isNewItem) {
        R.string.title_activity_term_new
    } else {
        R.string.title_activity_term_edit
    }

    override fun setupLayout() {
        mEditText = findViewById(R.id.editText) as EditText
        if (!mIsNew) {
            mEditText.setText(mItem!!.name)
        }

        setupDateTexts()
    }

    private fun setupDateTexts() {
        val today = LocalDate.now()
        mStartDate = mItem?.startDate ?: today
        mEndDate = mItem?.endDate ?: today.plusWeeks(12)

        mStartDateHelper = DateSelectorHelper(this, R.id.textView_start_date)
        mStartDateHelper.setup(mStartDate) { _, date ->
            mStartDate = date
            mStartDateHelper.updateDate(mStartDate)
        }

        mEndDateHelper = DateSelectorHelper(this, R.id.textView_end_date)
        mEndDateHelper.setup(mEndDate) { _, date ->
            mEndDate = date
            mEndDateHelper.updateDate(mEndDate)
        }
    }

    override fun handleDoneAction() {
        var newName = mEditText.text.toString()
        if (newName.isEmpty()) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_invalid_name,
                    Snackbar.LENGTH_SHORT).show()
            return
        }
        newName = newName.title()

        if (mStartDate == null || mEndDate == null) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_term_dates_required, Snackbar.LENGTH_SHORT).show()
            return
        }
        if (mStartDate == mEndDate) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_start_date_equal_end_date, Snackbar.LENGTH_SHORT).show()
            return
        }
        if (mStartDate!!.isAfter(mEndDate!!)) {
            Snackbar.make(findViewById(R.id.rootView),
                    R.string.message_start_date_after_end_date, Snackbar.LENGTH_SHORT).show()
            return
        }

        val id = if (mIsNew) {
            mDataHandler.getHighestItemId() + 1
        } else {
            mItem!!.id
        }

        mItem = Term(id, mTimetableId, newName, mStartDate!!, mEndDate!!)

        if (mIsNew) {
            mDataHandler.addItem(mItem!!)
        } else {
            mDataHandler.replaceItem(mItem!!.id, mItem!!)
        }

        setResult(Activity.RESULT_OK)
        supportFinishAfterTransition()
    }

    override fun handleDeleteAction() {
        mDataHandler.deleteItem(mItem!!.id)
        setResult(Activity.RESULT_OK)
        finish()
    }

}
