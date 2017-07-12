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

package co.timetableapp.ui.components

import android.app.Activity
import android.content.DialogInterface
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.SubjectHandler
import co.timetableapp.model.Subject
import co.timetableapp.ui.subjects.SubjectsAdapter

/**
 * A helper class for setting up a [TextView] to show and change the [Subject].
 */
class SubjectSelectorHelper(val activity: Activity, @IdRes val textViewResId: Int) {

    var onNewSubjectListener: DialogInterface.OnClickListener? = null
    var onSubjectChangeListener: OnSubjectChangeListener? = null

    private val mTextView = activity.findViewById(textViewResId) as TextView

    private var mSubjectDialog: AlertDialog? = null

    /**
     * Sets up this helper class, displaying the [subject] and preparing actions for when the
     * [TextView] is clicked.
     *
     * @param subject           the subject to initially display on the [TextView]. This can be
     *                          null, in which case a 'hint' text will be initially displayed.
     * @param allowNoSubject    whether a 'No subject' option should be shown in the subject
     *                          selector dialog.
     */
    @JvmOverloads
    fun setup(subject: Subject?, allowNoSubject: Boolean = false) {
        subject?.let { updateSubject(it) }
        setupOnClick(allowNoSubject)
    }

    private fun setupOnClick(allowNoSubject: Boolean) = mTextView.setOnClickListener {
        val builder = AlertDialog.Builder(activity)

        val subjects = SubjectHandler(activity).getItems(activity.application)
        subjects.sort()

        val adapter = SubjectsAdapter(activity, subjects)
        adapter.setOnItemClickListener { _, position ->
            updateSubject(subjects[position])
            mSubjectDialog?.dismiss()
        }

        val recyclerView = RecyclerView(activity)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        val titleView = activity.layoutInflater.inflate(R.layout.dialog_title_with_padding, null)
        (titleView.findViewById(R.id.title) as TextView).setText(R.string.choose_subject)

        builder.setView(recyclerView)
                .setCustomTitle(titleView)
                .setPositiveButton(R.string.action_new, onNewSubjectListener)

        if (allowNoSubject) {
            builder.setNeutralButton(R.string.no_subject) { _, _ ->
                updateSubject(null)
            }
        }

        mSubjectDialog = builder.create()
        mSubjectDialog!!.show()
    }

    /**
     * Updates the displayed text according to the [newSubject].
     *
     * [newSubject] can be null, in which case a 'hint' text is shown.
     */
    fun updateSubject(newSubject: Subject?) {
        mSubjectDialog?.dismiss() // in case it is already open

        if (newSubject == null) {
            mTextView.text = activity.getString(R.string.property_subject)
            mTextView.setTextColor(ContextCompat.getColor(activity, R.color.mdu_text_black_secondary))
        } else {
            mTextView.text = newSubject.name
            mTextView.setTextColor(ContextCompat.getColor(activity, R.color.mdu_text_black))
        }

        onSubjectChangeListener?.onSubjectChange(newSubject)
    }

    interface OnSubjectChangeListener {

        /**
         * Callback to be invoked when the [Subject] being displayed has changed.
         */
        fun onSubjectChange(subject: Subject?)

    }

}
