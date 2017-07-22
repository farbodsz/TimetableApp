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

package co.timetableapp.ui.subjects

import android.app.Activity
import android.content.Intent
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.SubjectHandler
import co.timetableapp.model.Color
import co.timetableapp.model.Subject
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.util.UiUtils
import co.timetableapp.util.title

/**
 * An activity for the user to create or edit a [Subject].
 *
 * @see SubjectsActivity
 */
class SubjectEditActivity : ItemEditActivity<Subject>() {

    private val mSubjectHandler = SubjectHandler(this)

    private lateinit var mNameEditText: EditText
    private lateinit var mAbbreviationEditText: EditText

    private lateinit var mColor: Color
    private lateinit var mColorDialog: AlertDialog

    override fun getLayoutResource() = R.layout.activity_subject_edit

    override fun getTitleRes(isNewItem: Boolean) = if (isNewItem) {
        R.string.title_activity_subject_new
    } else {
        R.string.title_activity_subject_edit
    }

    override fun setupLayout() {
        mNameEditText = findViewById(R.id.editText_name) as EditText
        if (!mIsNew) {
            mNameEditText.setText(mItem!!.name)
        }

        mAbbreviationEditText = findViewById(R.id.editText_abbreviation) as EditText
        if (!mIsNew) {
            mAbbreviationEditText.setText(mItem!!.abbreviation)
        }

        setupColorPicker()
    }

    private fun setupColorPicker() {
        mColor = Color(if (mIsNew) 6 else mItem!!.colorId)

        UiUtils.setBarColors(mColor, this, mToolbar!!)

        val imageView = findViewById(R.id.imageView) as ImageView
        imageView.setImageResource(mColor.getPrimaryColorResId(this))

        imageView.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            val colors = Color.getAllColors()

            val adapter = ColorsAdapter(baseContext, colors)
            adapter.onItemClick { _, position ->
                mColor = colors[position]
                imageView.setImageResource(mColor.getPrimaryColorResId(baseContext))
                UiUtils.setBarColors(mColor, this, mToolbar!!)
                mColorDialog.dismiss()
            }

            val recyclerView = RecyclerView(baseContext)
            with(recyclerView) {
                setHasFixedSize(true)
                layoutManager = GridLayoutManager(this@SubjectEditActivity,
                        resources.getInteger(R.integer.subject_color_dialog_columns))
                this.adapter = adapter
            }

            val titleView = layoutInflater.inflate(R.layout.dialog_title_with_padding, null)
            (titleView.findViewById(R.id.title) as TextView).setText(R.string.choose_color)

            builder.setView(recyclerView)
                    .setCustomTitle(titleView)

            mColorDialog = builder.create()
            mColorDialog.show()
        }
    }

    override fun handleDoneAction() {
        var newName = mNameEditText.text.toString()
        if (newName.isEmpty()) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_invalid_name,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        newName = newName.title()

        val newAbbreviation = mAbbreviationEditText.text.toString()

        if (mIsNew) {
            val currentTimetable = (application as TimetableApplication).currentTimetable!!

            mItem = Subject(mSubjectHandler.getHighestItemId() + 1,
                    currentTimetable.id,
                    newName,
                    newAbbreviation,
                    mColor.id)

            mSubjectHandler.addItem(mItem!!)

        } else {
            mItem!!.name = newName
            mItem!!.abbreviation = newAbbreviation
            mItem!!.colorId = mColor.id
            mSubjectHandler.replaceItem(mItem!!.id, mItem!!)
        }

        val intent = Intent()
        intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItem)
        setResult(Activity.RESULT_OK, intent)
        supportFinishAfterTransition()
    }

    override fun handleDeleteAction() {
        AlertDialog.Builder(this)
                .setTitle(R.string.delete_subject)
                .setMessage(R.string.delete_confirmation_subject)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    mSubjectHandler.deleteItemWithReferences(mItem!!.id)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

}
