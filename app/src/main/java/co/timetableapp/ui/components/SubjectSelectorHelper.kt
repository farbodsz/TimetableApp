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
     * @param subject   the subject to initially display on the [TextView]. This can be null, in
     *                  which case a placeholder 'hint' text will be initially displayed.
     */
    fun setup(subject: Subject?) {
        subject?.let { updateSubject(it) }
        setupOnClick()
    }

    private fun setupOnClick() = mTextView.setOnClickListener {
        val builder = AlertDialog.Builder(activity)

        val subjects = SubjectHandler(activity).getItems(activity.application)
        subjects.sort()

        val adapter = SubjectsAdapter(activity, subjects)
        adapter.setOnEntryClickListener { _, position ->
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

        mSubjectDialog = builder.create()
        mSubjectDialog!!.show()
    }

    fun updateSubject(newSubject: Subject) {
        mSubjectDialog?.dismiss() // in case it is already open

        mTextView.text = newSubject.name
        mTextView.setTextColor(ContextCompat.getColor(activity, R.color.mdu_text_black))

        onSubjectChangeListener?.onSubjectChange(newSubject)
    }

    interface OnSubjectChangeListener {

        /**
         * Callback to be invoked when the [Subject] being displayed has changed.
         */
        fun onSubjectChange(subject: Subject)

    }

}
