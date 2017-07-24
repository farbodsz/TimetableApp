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
import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.ExamHandler
import co.timetableapp.model.Color
import co.timetableapp.model.Exam
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils

/**
 * Shows the details of an exam.
 *
 * @see Exam
 * @see ExamEditActivity
 * @see ItemDetailActivity
 */
class ExamDetailActivity : ItemDetailActivity<Exam>() {

    override fun initializeDataHandler() = ExamHandler(this)

    override fun getLayoutResource() = R.layout.activity_exam_detail

    override fun onNullExtras() {
        val intent = Intent(this, ExamEditActivity::class.java)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun setupLayout() {
        setupToolbar()

        val dateFormatter = DateUtils.FORMATTER_FULL_DATE
        findViewById<TextView>(R.id.textView_date).text = mItem.date.format(dateFormatter)

        val timeText =
                "${mItem.startTime} - ${mItem.startTime.plusMinutes(mItem.duration.toLong())}"
        findViewById<TextView>(R.id.textView_times).text = timeText

        val seatGroup = findViewById<ViewGroup>(R.id.viewGroup_seat)
        if (mItem.hasSeat()) {
            seatGroup.visibility = View.VISIBLE
            findViewById<TextView>(R.id.textView_seat).text = mItem.seat
        } else {
            seatGroup.visibility = View.GONE
        }

        val roomGroup = findViewById<ViewGroup>(R.id.viewGroup_room)
        if (mItem.hasRoom()) {
            roomGroup.visibility = View.VISIBLE
            findViewById<TextView>(R.id.textView_room).text = mItem.room
        } else {
            roomGroup.visibility = View.GONE
        }

        findViewById<View>(R.id.location_divider).visibility =
                if (!mItem.hasRoom() && !mItem.hasSeat()) View.GONE else View.VISIBLE

        val viewGroupResit = findViewById<ViewGroup>(R.id.viewGroup_resit)
        viewGroupResit.visibility = if (mItem.resit) View.VISIBLE else View.GONE

        val notesText = findViewById<TextView>(R.id.textView_notes)
        UiUtils.formatNotesTextView(this, notesText, mItem.notes)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        val subject = Subject.create(this, mItem.subjectId)
        findViewById<TextView>(R.id.title).text = mItem.makeName(subject)

        val color = Color(subject.colorId)
        UiUtils.setBarColors(color, this, toolbar)
    }

    override fun onMenuEditClick() {
        val intent = Intent(this, ExamEditActivity::class.java)
        intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItem)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    override fun saveEditsAndClose() {
        val intent = Intent().putExtra(EXTRA_ITEM, mItem)
        setResult(Activity.RESULT_OK, intent) // to reload any changes in ExamsActivity
        supportFinishAfterTransition()
    }

    override fun saveDeleteAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ExamsActivity
        finish()
    }

}
