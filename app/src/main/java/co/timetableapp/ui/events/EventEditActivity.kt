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

package co.timetableapp.ui.events

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.EditText
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.EventHandler
import co.timetableapp.model.Color
import co.timetableapp.model.Event
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.ui.components.DateSelectorHelper
import co.timetableapp.ui.components.SubjectSelectorHelper
import co.timetableapp.ui.subjects.SubjectEditActivity
import co.timetableapp.util.UiUtils
import co.timetableapp.util.title
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Allows the user to edit an [Event]
 *
 * @see ItemEditActivity
 */
class EventEditActivity : ItemEditActivity<Event>() {

    companion object {
        private val REQUEST_CODE_SUBJECT_DETAIL = 2
    }

    private val mDataHandler = EventHandler(this)

    private lateinit var mEditTextTitle: EditText
    private lateinit var mEditTextDetail: EditText
    private lateinit var mEditTextLocation: EditText

    private lateinit var mEventDate: LocalDate
    private lateinit var mDateHelper: DateSelectorHelper

    private var mStartTime: LocalTime? = null
    private lateinit var mStartTimeText: TextView

    private var mEndTime: LocalTime? = null
    private lateinit var mEndTimeText: TextView

    private var mSubject: Subject? = null
    private lateinit var mSubjectHelper: SubjectSelectorHelper

    override fun getLayoutResource() = R.layout.activity_event_edit

    override fun getTitleRes(isNewItem: Boolean) = if (isNewItem) {
        R.string.title_activity_event_new
    } else {
        R.string.title_activity_event_edit
    }

    override fun setupLayout() {
        mEditTextTitle = findViewById(R.id.editText_title) as EditText
        if (!mIsNew) {
            mEditTextTitle.setText(mItem!!.title)
        }

        mEditTextDetail = findViewById(R.id.editText_detail) as EditText
        if (!mIsNew) {
            mEditTextDetail.setText(mItem!!.notes)
        }

        mEditTextLocation = findViewById(R.id.editText_location) as EditText
        if (!mIsNew) {
            mEditTextLocation.setText(mItem!!.location)
        }

        setupSubjectHelper()

        setupDateText()
        setupStartTimeText()
        setupEndTimeText()
    }

    private fun setupSubjectHelper() {
        mSubjectHelper = SubjectSelectorHelper(this, R.id.textView_subject)

        mSubjectHelper.onCreateNewSubject { _, _ ->
            val intent = Intent(this, SubjectEditActivity::class.java)
            ActivityCompat.startActivityForResult(
                    this,
                    intent,
                    REQUEST_CODE_SUBJECT_DETAIL,
                    null
            )
        }

        mSubjectHelper.onSubjectChange {
            mSubject = it

            val color: Color
            if (it == null) {
                color = Event.DEFAULT_COLOR
            } else {
                color = Color(it.colorId)
            }

            UiUtils.setBarColors(
                    color,
                    this@EventEditActivity,
                    mToolbar!!,
                    findViewById(R.id.appBarLayout),
                    findViewById(R.id.toolbar_container)
            )
        }

        mSubjectHelper.setup(mItem?.getRelatedSubject(this), true)
    }

    private fun setupDateText() {
        mEventDate = mItem?.startDateTime?.toLocalDate() ?: LocalDate.now()

        mDateHelper = DateSelectorHelper(this, R.id.textView_date)
        mDateHelper.setup(mEventDate) { _, date ->
            mEventDate = date
            mDateHelper.updateDate(mEventDate)
        }
    }

    private fun setupStartTimeText() {
        mStartTimeText = findViewById(R.id.textView_start_time) as TextView

        if (!mIsNew) {
            mStartTime = mItem!!.startDateTime.toLocalTime()
            updateTimeTexts()
        }

        mStartTimeText.setOnClickListener {
            var initialHour = 9
            var initialMinute = 0
            if (mStartTime != null) {
                initialHour = mStartTime!!.hour
                initialMinute = mStartTime!!.minute
            }

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mStartTime = LocalTime.of(hour, minute)
                updateTimeTexts()
            }, initialHour, initialMinute, true).show()
        }
    }

    private fun setupEndTimeText() {
        mEndTimeText = findViewById(R.id.textView_end_time) as TextView

        if (!mIsNew) {
            mEndTime = mItem!!.endDateTime.toLocalTime()
            updateTimeTexts()
        }

        mEndTimeText.setOnClickListener {
            var initialHour = 9
            var initialMinute = 0
            if (mEndTime != null) {
                initialHour = mEndTime!!.hour
                initialMinute = mEndTime!!.minute
            }

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mEndTime = LocalTime.of(hour, minute)
                updateTimeTexts()
            }, initialHour, initialMinute, true).show()
        }
    }

    private fun updateTimeTexts() {
        if (mStartTime != null) {
            mStartTimeText.text = mStartTime!!.toString()
            mStartTimeText.setTextColor(ContextCompat.getColor(baseContext, R.color.mdu_text_black))
        }

        if (mEndTime != null) {
            mEndTimeText.text = mEndTime!!.toString()
            mEndTimeText.setTextColor(ContextCompat.getColor(baseContext, R.color.mdu_text_black))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SUBJECT_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                mSubject = data.getParcelableExtra<Subject>(ItemEditActivity.EXTRA_ITEM)
                mSubjectHelper.updateSubject(mSubject)
            }
        }
    }

    override fun handleDoneAction() {
        var newTitle = mEditTextTitle.text.toString()
        newTitle = newTitle.title()

        val newDetail = mEditTextDetail.text.toString().trim { it <= ' ' }
        val newLocation = mEditTextLocation.text.toString().trim { it <= ' ' }

        if (newTitle.trim { it <= ' ' }.isEmpty()) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_title_required,
                    Snackbar.LENGTH_SHORT).show()
            return
        }

        if (mStartTime == null || mEndTime == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_times_required,
                    Snackbar.LENGTH_SHORT).show()
            return
        }

        val id = if (mIsNew) mDataHandler.getHighestItemId() + 1 else mItem!!.id

        val timetableId = (application as TimetableApplication).currentTimetable!!.id

        mItem = Event(
                id,
                timetableId,
                newTitle,
                newDetail,
                LocalDateTime.of(mEventDate, mStartTime!!),
                LocalDateTime.of(mEventDate, mEndTime!!),
                newLocation,
                if (mSubject == null) 0 else mSubject!!.id
        )

        if (mIsNew) {
            mDataHandler.addItem(mItem!!)
        } else {
            mDataHandler.replaceItem(mItem!!.id, mItem!!)
        }

        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        supportFinishAfterTransition()
    }

    override fun handleDeleteAction() {
        AlertDialog.Builder(this)
                .setTitle(R.string.delete_exam)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    mDataHandler.deleteItem(mItem!!.id)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

}
