package co.timetableapp.ui

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.Toolbar
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.data.handler.ClassDetailHandler
import co.timetableapp.data.handler.ClassHandler
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.framework.Class
import co.timetableapp.framework.ClassTime
import co.timetableapp.framework.Color
import co.timetableapp.framework.Subject
import co.timetableapp.util.UiUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * Shows the details of a class.
 *
 * @see Class
 * @see ClassesActivity
 * @see ClassEditActivity
 * @see ItemDetailActivity
 */
class ClassDetailActivity : ItemDetailActivity<Class>() {

    override fun initializeDataHandler() = ClassHandler(this)

    override fun getLayoutResource() = R.layout.activity_class_detail

    override fun onNullExtras() {
        val intent = Intent(this, co.timetableapp.ui.ClassEditActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_ITEM_EDIT)
    }

    override fun setupLayout() {
        val subject = Subject.create(this, mItem!!.subjectId)!!
        setupToolbar(subject)

        val locationBuilder = StringBuilder()
        val teacherBuilder = StringBuilder()

        val allClassTimes = ArrayList<ClassTime>()

        ClassDetailHandler.getClassDetailsForClass(this, mItem!!.id).forEach { classDetail ->
            classDetail.formatLocationName()?.let {
                locationBuilder.append(it).append("\n")
            }

            if (classDetail.hasTeacher()) {
                teacherBuilder.append(classDetail.teacher).append("\n")
            }

            allClassTimes.addAll(ClassTimeHandler.getClassTimesForDetail(this, classDetail.id))
        }

        val locationText = findViewById(R.id.textView_location) as TextView
        locationText.text = locationBuilder.toString().removeSuffix("\n")

        val teacherText = findViewById(R.id.textView_teacher) as TextView
        teacherText.text = teacherBuilder.toString().removeSuffix("\n")

        val classTimesText = findViewById(R.id.textView_times) as TextView
        classTimesText.text = produceClassTimesText(allClassTimes)
    }

    private fun setupToolbar(subject: Subject) {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        supportActionBar!!.title = mItem!!.makeName(subject)

        val color = Color(subject.colorId)
        UiUtils.setBarColors(color, this, toolbar)
    }

    private fun produceClassTimesText(classTimes: ArrayList<ClassTime>): String {
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

            stringBuilder.append(", ")
                    .append(it.startTime.toString())
                    .append(" - ")
                    .append(it.endTime.toString())
                    .append("\n")
        }

        return stringBuilder.toString().removeSuffix("\n")
    }

    override fun onMenuEditClick() {
        val intent = Intent(this, co.timetableapp.ui.ClassEditActivity::class.java)
        intent.putExtra(co.timetableapp.ui.ClassEditActivity.EXTRA_CLASS, mItem)
        startActivityForResult(intent, REQUEST_CODE_ITEM_EDIT)

    }

    override fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        supportFinishAfterTransition()
    }

    override fun saveEditsAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ClassesActivity
        supportFinishAfterTransition()
    }

    override fun saveDeleteAndClose() {
        setResult(Activity.RESULT_OK) // to reload any changes in ClassesActivity
        finish()
    }

}
