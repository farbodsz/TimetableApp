package com.satsumasoftware.timetable.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.db.handler.ClassDetailHandler
import com.satsumasoftware.timetable.db.handler.ClassHandler
import com.satsumasoftware.timetable.db.handler.ClassTimeHandler
import com.satsumasoftware.timetable.framework.Class
import com.satsumasoftware.timetable.framework.ClassDetail
import com.satsumasoftware.timetable.framework.Color
import com.satsumasoftware.timetable.framework.Subject
import com.satsumasoftware.timetable.ui.ClassDetailActivity.Companion.EXTRA_CLASS
import com.satsumasoftware.timetable.util.UiUtils
import java.util.*

/**
 * Shows the details of a class.
 *
 * The details are displayed to the user but they cannot be edited here and must be done in
 * [ClassEditActivity].
 *
 * Additionally, this activity should be invoked to create a new class, passing no intent
 * data so that [EXTRA_CLASS] is null.
 *
 * @see Class
 * @see ClassesActivity
 * @see ClassEditActivity
 */
class ClassDetailActivity : AppCompatActivity() {

    companion object {

        private const val LOG_TAG = "ClassDetailActivity"

        /**
         * The key for the [Class] passed through an intent extra.

         * It should be null if we're creating a new class.
         */
        internal const val EXTRA_CLASS = "extra_class"

        private const val REQUEST_CODE_CLASS_EDIT = 1
    }

    private var mClass: Class? = null

    private var mIsNew: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_detail)

        val extras = intent.extras
        if (extras == null) {
            // No class to display - assume we mean to create a new one
            mIsNew = true
            val intent = Intent(this, ClassEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CLASS_EDIT)
            return
        }

        mClass = extras.getParcelable(EXTRA_CLASS)

        setupLayout()
    }

    private fun setupLayout() {
        val subject = Subject.create(this, mClass!!.subjectId)!!
        setupToolbar(subject)

        val locationBuilder = StringBuilder()
        val teacherBuilder = StringBuilder()
        val classTimesBuilder = StringBuilder()

        ClassDetailHandler.getClassDetailsForClass(this, mClass!!.id).forEach { classDetail ->
            classDetail.formatLocationName()?.let {
                locationBuilder.append(it).append("\n")
            }

            if (classDetail.hasTeacher()) {
                teacherBuilder.append(classDetail.teacher).append("\n")
            }

            classTimesBuilder.append(produceClassTimesText(classDetail))
        }

        val locationText = findViewById(R.id.textView_location) as TextView
        locationText.text = locationBuilder.toString().removeSuffix("\n")

        val teacherText = findViewById(R.id.textView_teacher) as TextView
        teacherText.text = teacherBuilder.toString().removeSuffix("\n")

        val classTimesText = findViewById(R.id.textView_times) as TextView
        classTimesText.text = classTimesBuilder.toString().removeSuffix("\n")
    }

    private fun setupToolbar(subject: Subject) {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        supportActionBar!!.title = Class.makeName(mClass!!, subject)

        val color = Color(subject.colorId)
        UiUtils.setBarColors(color, this, toolbar)
    }

    private fun produceClassTimesText(classDetail: ClassDetail): StringBuilder {
        val classTimes = ClassTimeHandler.getClassTimesForDetail(this, classDetail.id)

        Collections.sort(classTimes) { classTime1, classTime2 ->
            // Sort by day, then by time
            val dayComparison = classTime1.day.compareTo(classTime2.day)
            if (dayComparison != 0) {
                dayComparison
            } else {
                classTime1.startTime.compareTo(classTime2.startTime)
            }
        }

        val stringBuilder = StringBuilder()

        classTimes.forEach {
            val dayString = it.day.toString()
            val formattedDayString =
                    dayString.substring(0, 1).toUpperCase() + dayString.substring(1).toLowerCase()
            stringBuilder.append(formattedDayString)

            val weekText = it.getWeekText(this)
            if (weekText.isNotEmpty()) {
                stringBuilder.append(" ")
                        .append(weekText)
            }

            stringBuilder.append(it.getWeekText(this))
                    .append(", ")
                    .append(it.startTime.toString())
                    .append(" - ")
                    .append(it.endTime.toString())
                    .append("\n")
        }

        return stringBuilder
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CLASS_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the edited class (it would have the highest id if it's new)
                val editedClassId = if (mIsNew) {
                    ClassHandler(this).getHighestItemId()
                } else {
                    mClass!!.id
                }
                mClass = Class.create(this, editedClassId)

                if (mClass == null) {
                    Log.v(LOG_TAG, "Class is null - must have been deleted")
                    saveDeleteAndClose()
                    return
                }

                if (mIsNew) {
                    saveEditsAndClose()
                } else {
                    setupLayout()
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (mIsNew) {
                    cancelAndClose()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_item_detail, menu)
        UiUtils.tintMenuIcons(this, menu!!, R.id.action_edit)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_edit -> {
                val intent = Intent(this, ClassEditActivity::class.java)
                intent.putExtra(ClassEditActivity.EXTRA_CLASS, mClass)
                startActivityForResult(intent, REQUEST_CODE_CLASS_EDIT)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = saveEditsAndClose()

    private fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    private fun saveEditsAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ClassesActivity
        supportFinishAfterTransition()
    }

    private fun saveDeleteAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ClassesActivity
        finish()
    }

}
