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

package co.timetableapp.ui.assignments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.AssignmentHandler
import co.timetableapp.data.handler.ClassHandler
import co.timetableapp.model.Assignment
import co.timetableapp.model.Class
import co.timetableapp.model.Color
import co.timetableapp.model.Subject
import co.timetableapp.ui.classes.ClassesAdapter
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import co.timetableapp.util.title
import org.threeten.bp.LocalDate

/**
 * An activity for the user to edit or create [assignments][Assignment].
 */
class AssignmentEditActivity : AppCompatActivity() {

    companion object {

        /**
         * The key for the [Assignment] passed through an intent extra.
         *
         * It should be null if we're creating a new assignment.
         */
        const val EXTRA_ASSIGNMENT = "extra_assignment"
    }

    private var mAssignment: Assignment? = null
    private var mIsNew = false

    private val mAssignmentHandler = AssignmentHandler(this)

    private lateinit var mToolbar: Toolbar

    private lateinit var mTitleEditText: EditText
    private lateinit var mDetailEditText: EditText

    private var mClass: Class? = null
    private lateinit var mClassText: TextView
    private lateinit var mClassDialog: AlertDialog

    private var mDueDate: LocalDate? = null
    private lateinit var mDateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assignment_edit)

        mToolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(mToolbar)

        val extras = intent.extras
        extras?.let {
            mAssignment = it.getParcelable<Assignment>(EXTRA_ASSIGNMENT)
        }
        mIsNew = mAssignment == null

        val titleResId = if (mIsNew) {
            R.string.title_activity_assignment_new
        } else {
            R.string.title_activity_assignment_edit
        }
        supportActionBar!!.title = (resources.getString(titleResId))

        mToolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_close_black_24dp)
        mToolbar.setNavigationOnClickListener { handleCloseAction() }

        setupLayout()
    }

    private fun setupLayout() {
        mTitleEditText = findViewById(R.id.editText_title) as EditText
        if (!mIsNew) {
            mTitleEditText.setText(mAssignment!!.title)
        }

        mDetailEditText = findViewById(R.id.editText_detail) as EditText
        if (!mIsNew) {
            mDetailEditText.setText(mAssignment!!.detail)
        }

        setupClassText()
        setupDateText()
    }

    private fun setupClassText() {
        mClassText = findViewById(R.id.textView_class) as TextView

        if (!mIsNew) {
            mClass = Class.create(this, mAssignment!!.classId)
            updateLinkedClass()
        }

        mClassText.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            val classes = ClassHandler(this).getItems(application)

            classes.sortWith(Class.NaturalSortComparator(baseContext))

            val adapter = ClassesAdapter(baseContext, classes)
            adapter.onItemClick { _, position ->
                mClass = classes[position]
                updateLinkedClass()
                mClassDialog.dismiss()
            }

            val recyclerView = RecyclerView(baseContext)
            with(recyclerView) {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@AssignmentEditActivity)
                this.adapter = adapter
            }

            val titleView = layoutInflater.inflate(R.layout.dialog_title_with_padding, null)
            (titleView.findViewById(R.id.title) as TextView).setText(R.string.choose_class)

            builder.setView(recyclerView)
                    .setCustomTitle(titleView)

            mClassDialog = builder.create()
            mClassDialog.show()
        }
    }

    private fun setupDateText() {
        mDateText = findViewById(R.id.textView_date) as TextView

        if (!mIsNew) {
            mDueDate = mAssignment!!.dueDate
            updateDateText()
        }

        mDateText.setOnClickListener {
            // Note: -1 and +1s in code because Android month values are from 0-11 (to
            // correspond with java.util.Calendar) but LocalDate month values are from 1-12

            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mDueDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateDateText()
            }

            DatePickerDialog(
                    this@AssignmentEditActivity,
                    listener,
                    if (mIsNew) LocalDate.now().year else mDueDate!!.year,
                    if (mIsNew) LocalDate.now().monthValue - 1 else mDueDate!!.monthValue - 1,
                    if (mIsNew) LocalDate.now().dayOfMonth else mDueDate!!.dayOfMonth
            ).show()
        }
    }

    private fun updateLinkedClass() {
        val subject = Subject.create(baseContext, mClass!!.subjectId)

        mClassText.text = subject.name
        mClassText.setTextColor(ContextCompat.getColor(baseContext, R.color.mdu_text_black))

        val color = Color(subject.colorId)
        UiUtils.setBarColors(
                color,
                this,
                mToolbar,
                findViewById(R.id.appBarLayout),
                findViewById(R.id.toolbar_container))
    }

    private fun updateDateText() {
        mDateText.text = mDueDate!!.format(DateUtils.FORMATTER_FULL_DATE)
        mDateText.setTextColor(ContextCompat.getColor(baseContext, R.color.mdu_text_black))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_edit, menu)
        UiUtils.tintMenuIcons(this, menu!!, R.id.action_done)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        if (mIsNew) {
            menu!!.findItem(R.id.action_delete).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_done -> handleDoneAction()
            R.id.action_delete -> handleDeleteAction()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        handleCloseAction()
    }

    private fun handleCloseAction() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun handleDoneAction() {
        var newTitle = mTitleEditText.text.toString()
        Log.d("newTitle", "=> $newTitle")
        if (newTitle.isEmpty()) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_title_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        newTitle = newTitle.title()

        if (mClass == null) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_class_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if (mDueDate == null) {
            Snackbar.make(findViewById(R.id.rootView), R.string.message_due_date_required,
                    Snackbar.LENGTH_SHORT).show()
            return
        }

        val assignmentId = if (mIsNew) {
            mAssignmentHandler.getHighestItemId() + 1
        } else {
            mAssignment!!.id
        }

        val completionProgress = if (mIsNew) 0 else mAssignment!!.completionProgress

        val timetableId = (application as TimetableApplication).currentTimetable!!.id

        mAssignment = Assignment(
                assignmentId,
                timetableId,
                mClass!!.id,
                newTitle,
                mDetailEditText.text.toString(),
                mDueDate!!,
                completionProgress)

        if (mIsNew) {
            mAssignmentHandler.addItem(mAssignment!!)
        } else {
            mAssignmentHandler.replaceItem(mAssignment!!.id, mAssignment!!)
        }

        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun handleDeleteAction() {
        AlertDialog.Builder(this)
                .setTitle(R.string.delete_assignment)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    mAssignmentHandler.deleteItem(mAssignment!!.id)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

}
