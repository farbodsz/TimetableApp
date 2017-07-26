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

package co.timetableapp.ui.exams

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.CheckBox
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.Color
import co.timetableapp.model.Exam
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.ui.components.DateSelectorHelper
import co.timetableapp.ui.components.SubjectSelectorHelper
import co.timetableapp.ui.subjects.SubjectEditActivity
import co.timetableapp.util.UiUtils
import co.timetableapp.util.title
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

/**
 * Allows the user to edit an [Exam].
 *
 * @see ExamDetailActivity
 * @see ItemEditActivity
 */
class ExamEditActivity : ItemEditActivity<Exam>() {

    companion object {

        private const val REQUEST_CODE_SUBJECT_DETAIL = 2

        private const val NO_DURATION = -1
    }

    private val mDataHandler = ExamHandler(this)

    private lateinit var mEditTextModule: EditText
    private lateinit var mEditTextSeat: EditText
    private lateinit var mEditTextRoom: EditText

    private var mSubject: Subject? = null
    private lateinit var mSubjectHelper: SubjectSelectorHelper

    private lateinit var mExamDate: LocalDate
    private lateinit var mDateHelper: DateSelectorHelper

    private var mExamTime: LocalTime? = null
    private lateinit var mTimeText: TextView

    private var mExamDuration = NO_DURATION
    private lateinit var mDurationText: TextView
    private lateinit var mDurationDialog: AlertDialog

    private var mExamIsResit: Boolean = false

    private lateinit var mEditTextNotes: EditText

    override fun getLayoutResource() = R.layout.activity_exam_edit

    override fun getTitleRes(isNewItem: Boolean) = if (isNewItem) {
        R.string.title_activity_exam_new
    } else {
        R.string.title_activity_exam_edit
    }

    override fun setupLayout() {
        mEditTextModule = findViewById(R.id.editText_module) as EditText
        if (!mIsNew) {
            mEditTextModule.setText(mItem!!.moduleName)
        }

        mEditTextSeat = findViewById(R.id.editText_seat) as EditText
        if (!mIsNew) {
            mEditTextSeat.setText(mItem!!.seat)
        }

        mEditTextRoom = findViewById(R.id.editText_room) as EditText
        if (!mIsNew) {
            mEditTextRoom.setText(mItem!!.room)
        }

        mEditTextNotes = findViewById(R.id.editText_notes) as EditText
        if (!mIsNew) {
            mEditTextNotes.setText(mItem!!.notes)
        }

        setupSubjectHelper()

        setupDateText()
        setupTimeText()
        setupDurationText()

        setupResitCheckbox()
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
            mSubject = it!!  // exams must have related subjects - it can't be null
            val color = Color(it.colorId)
            UiUtils.setBarColors(
                    color,
                    this,
                    mToolbar!!,
                    findViewById(R.id.appBarLayout),
                    findViewById(R.id.toolbar_container)
            )
        }

        mSubjectHelper.setup(mItem?.getRelatedSubject(this))
    }

    private fun setupDateText() {
        mExamDate = mItem?.date ?: LocalDate.now()

        mDateHelper = DateSelectorHelper(this, R.id.textView_date)
        mDateHelper.setup(mExamDate) { _, date ->
            mExamDate = date
            mDateHelper.updateDate(mExamDate)
        }
    }

    private fun setupTimeText() {
        mTimeText = findViewById(R.id.textView_start_time) as TextView

        if (!mIsNew) {
            mExamTime = mItem!!.startTime
            updateTimeText()
        }

        mTimeText.setOnClickListener {
            var initialHour = 9
            var initialMinute = 0
            if (mExamTime != null) {
                initialHour = mExamTime!!.hour
                initialMinute = mExamTime!!.minute
            }

            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mExamTime = LocalTime.of(hour, minute)
                updateTimeText()
            }, initialHour, initialMinute, true).show()
        }
    }

    private fun updateTimeText() {
        mTimeText.text = mExamTime!!.toString()
        mTimeText.setTextColor(ContextCompat.getColor(baseContext, R.color.mdu_text_black))
    }

    private fun setupDurationText() {
        mDurationText = findViewById(R.id.textView_duration) as TextView

        if (!mIsNew) {
            mExamDuration = mItem!!.duration
            updateDurationText()
        }

        mDurationText.setOnClickListener {
            val builder = AlertDialog.Builder(this@ExamEditActivity)

            val numberPicker = NumberPicker(baseContext)
            numberPicker.minValue = 10
            numberPicker.maxValue = 360
            numberPicker.value = if (mExamDuration == NO_DURATION) 60 else mExamDuration

            val titleView = layoutInflater.inflate(R.layout.dialog_title_with_padding, null)
            (titleView.findViewById(R.id.title) as TextView).setText(R.string.property_duration)

            builder.setView(numberPicker)
                    .setCustomTitle(titleView)
                    .setPositiveButton(R.string.action_done) { _, _ ->
                        mExamDuration = numberPicker.value
                        updateDurationText()

                        mDurationDialog.dismiss()
                    }

            mDurationDialog = builder.create()
            mDurationDialog.show()
        }
    }

    private fun updateDurationText() {
        mDurationText.text = "$mExamDuration mins"
        mDurationText.setTextColor(ContextCompat.getColor(baseContext, R.color.mdu_text_black))
    }

    private fun setupResitCheckbox() {
        mExamIsResit = !mIsNew && mItem!!.resit

        val checkBox = findViewById(R.id.checkBox_resit) as CheckBox
        checkBox.isChecked = mExamIsResit
        checkBox.setOnCheckedChangeListener { _, isChecked -> mExamIsResit = isChecked }
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
        var newModuleName = mEditTextModule.text.toString()
        newModuleName = newModuleName.title()

        if (mSubject == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_subject_required,
                    Snackbar.LENGTH_SHORT).show()
            return
        }

        if (mExamTime == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_exam_time_required,
                    Snackbar.LENGTH_SHORT).show()
            return
        }

        if (mExamDuration == -1) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_exam_duration_required,
                    Snackbar.LENGTH_SHORT).show()
            return
        }

        val id = if (mIsNew) mDataHandler.getHighestItemId() + 1 else mItem!!.id

        val timetableId = (application as TimetableApplication).currentTimetable!!.id

        mItem = Exam(
                id,
                timetableId,
                mSubject!!.id,
                newModuleName,
                mExamDate,
                mExamTime!!,
                mExamDuration,
                mEditTextSeat.text.toString().trim(),
                mEditTextRoom.text.toString().trim(),
                mExamIsResit,
                mEditTextNotes.text.toString().trim()
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
