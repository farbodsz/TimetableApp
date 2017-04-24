package co.timetableapp.ui.classes

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.*
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.AssignmentsSchema
import co.timetableapp.data.schema.ExamsSchema
import co.timetableapp.model.Class
import co.timetableapp.model.ClassTime
import co.timetableapp.model.Color
import co.timetableapp.model.Subject
import co.timetableapp.ui.agenda.AgendaActivity
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.ui.components.CardOfItems
import co.timetableapp.util.UiUtils
import org.threeten.bp.format.DateTimeFormatter

/**
 * Shows the details of a class.
 *
 * @see Class
 * @see ClassesActivity
 * @see ClassEditActivity
 * @see ItemDetailActivity
 */
class ClassDetailActivity : ItemDetailActivity<Class>() {

    private var mColor: Color? = null

    override fun initializeDataHandler() = ClassHandler(this)

    override fun getLayoutResource() = R.layout.activity_class_detail

    override fun onNullExtras() {
        val intent = Intent(this, ClassEditActivity::class.java)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
    }

    override fun setupLayout() {
        setupToolbar()
        setupClassDetailCard()

        findViewById(R.id.main_card).setBackgroundColor(
                ContextCompat.getColor(this, mColor!!.getLightAccentColorRes(this)))

        setupRelatedItemCards()
    }

    private fun setupToolbar() {
        val subject = Subject.create(this, mItem!!.subjectId)!!

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        toolbar.navigationIcon = UiUtils.tintDrawable(this, R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener { saveEditsAndClose() }

        supportActionBar!!.title = mItem!!.makeName(subject)

        mColor = Color(subject.colorId)
        UiUtils.setBarColors(mColor!!, this, toolbar)
    }

    /**
     * Adds content to a card displaying details about the class (such as locations, teachers, and
     * the times of the class).
     */
    private fun setupClassDetailCard() {
        val locationBuilder = StringBuilder()
        val teacherBuilder = StringBuilder()
        val allClassTimes = ArrayList<ClassTime>()

        // Add locations, teachers, and times to StringBuilders or ArrayLists, with no duplicates
        ClassDetailHandler.getClassDetailsForClass(this, mItem!!.id).forEach { classDetail ->
            classDetail.formatLocationName()?.let {
                if (!locationBuilder.contains(it)) {
                    locationBuilder.append(it).append("\n")
                }
            }

            if (classDetail.hasTeacher()) {
                if (!teacherBuilder.contains(classDetail.teacher)) {
                    teacherBuilder.append(classDetail.teacher).append("\n")
                }
            }

            allClassTimes.addAll(ClassTimeHandler.getClassTimesForDetail(this, classDetail.id))
        }

        setClassDetailTexts(
                locationBuilder.toString().removeSuffix("\n"),
                teacherBuilder.toString().removeSuffix("\n"),
                produceClassTimesText(allClassTimes))
    }

    /**
     * @return the list of class times as a formatted string
     */
    private fun produceClassTimesText(classTimes: ArrayList<ClassTime>): String {
        classTimes.sort()
        val stringBuilder = StringBuilder()

        // Add all time texts - not expecting duplicate values for times
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

    /**
     * Sets the text on the TextViews for the class detail data.
     * Appropriate parts of the UI will not be displayed (i.e. when there is no text for an item).
     *
     * @param locations text to be displayed for the class locations
     * @param teachers text to be displayed for the teachers of the class
     * @param classTimes text to be displayed for the class times
     */
    private fun setClassDetailTexts(locations: String, teachers: String, classTimes: String) {
        val locationVisibility = if (locations.isEmpty()) View.GONE else View.VISIBLE
        findViewById(R.id.viewGroup_location).visibility = locationVisibility
        if (locations.isNotEmpty()) {
            (findViewById(R.id.textView_location) as TextView).text = locations
        }

        val teacherVisibility = if (teachers.isEmpty()) View.GONE else View.VISIBLE
        findViewById(R.id.viewGroup_teacher).visibility = teacherVisibility
        if (teachers.isNotEmpty()) {
            (findViewById(R.id.textView_teacher) as TextView).text = teachers
        }

        // No need to check if it's empty - all class details must have a class time
        val textViewTimes = findViewById(R.id.textView_times) as TextView
        textViewTimes.text = classTimes
    }

    /**
     * Sets up the UI for cards displaying items related to this class, such as assignments and
     * exams for this class.
     */
    private fun setupRelatedItemCards() {
        val cardContainer = findViewById(R.id.card_container) as LinearLayout

        val assignmentsCard = CardOfItems.Builder(this, cardContainer)
                .setTitle(R.string.title_assignments)
                .setItems(createAssignmentItems())
                .setItemsIconResource(R.drawable.ic_homework_black_24dp)
                .setButtonProperties(R.string.view_all, View.OnClickListener {
                    startActivity(Intent(this@ClassDetailActivity, AgendaActivity::class.java))
                })
                .build()
                .view
        cardContainer.addView(assignmentsCard)

        val examsCard = CardOfItems.Builder(this, cardContainer)
                .setTitle(R.string.title_exams)
                .setItems(createExamItems())
                .setItemsIconResource(R.drawable.ic_assessment_black_24dp)
                .setButtonProperties(R.string.view_all, View.OnClickListener {
                    startActivity(Intent(this@ClassDetailActivity, AgendaActivity::class.java))
                })
                .build()
                .view
        cardContainer.addView(examsCard)
    }

    private fun createAssignmentItems(): ArrayList<CardOfItems.CardItem> {
        val timetableId = (application as TimetableApplication).currentTimetable!!.id

        // Get assignments for this class
        val query = Query.Builder()
                .addFilter(Filters.equal(AssignmentsSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .addFilter(Filters.equal(AssignmentsSchema.COL_CLASS_ID, mItem!!.id.toString()))
                .build()

        // Create items
        val items = ArrayList<CardOfItems.CardItem>()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM uuuu")
        AssignmentHandler(this).getAllItems(query).forEach {
            if (it.isUpcoming() || it.isOverdue()) {
                items.add(CardOfItems.CardItem(
                        it.title,
                        it.dueDate.format(formatter),
                        null))
            }
        }

        return items
    }

    private fun createExamItems(): ArrayList<CardOfItems.CardItem> {
        val timetableId = (application as TimetableApplication).currentTimetable!!.id

        // Get exams for this class (actually for the subject of the class)
        val query = Query.Builder()
                .addFilter(Filters.equal(ExamsSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .addFilter(Filters.equal(ExamsSchema.COL_SUBJECT_ID, mItem!!.subjectId.toString()))
                .build()

        // Create items
        val items = ArrayList<CardOfItems.CardItem>()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM uuuu")
        val subject = Subject.create(this, mItem!!.subjectId)!!
        ExamHandler(this).getAllItems(query).forEach {
            if (it.isUpcoming()) {
                items.add(CardOfItems.CardItem(
                        it.makeName(subject),
                        it.date.format(formatter),
                        null))
            }
        }

        return items
    }

    override fun onMenuEditClick() {
        val intent = Intent(this, ClassEditActivity::class.java)
        intent.putExtra(ItemEditActivity.EXTRA_ITEM, mItem)
        ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_ITEM_EDIT, null)
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
