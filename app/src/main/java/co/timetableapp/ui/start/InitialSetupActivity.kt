package co.timetableapp.ui.start

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.SubjectHandler
import co.timetableapp.data.handler.TimetableHandler
import co.timetableapp.model.Subject
import co.timetableapp.model.Timetable
import co.timetableapp.ui.home.MainActivity
import co.timetableapp.util.PrefUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * This activity guides the user through setting up their new timetable.
 */
class InitialSetupActivity : AppCompatActivity() {

    private val mViewPager by lazy { findViewById(R.id.viewPager) as ViewPager }

    private lateinit var mProgressText: TextView
    private lateinit var mPrevButton: Button
    private lateinit var mNextButton: Button

    /**
     * Contains static variables for setting up the timetable.
     */
    private companion object {
        val sTimetableBuilder = TimetableBuilder()
        var sSubjectText: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)

        setupLayout()
    }

    private fun setupLayout() {
        mViewPager.adapter = PagerAdapter(supportFragmentManager)

        mProgressText = findViewById(R.id.textView_progress) as TextView
        updateProgressText()

        mPrevButton = findViewById(R.id.button_previous) as Button
        mPrevButton.setOnClickListener {
            changePage(true)
            updateProgressText()
        }

        mNextButton = findViewById(R.id.button_next) as Button
        mNextButton.setOnClickListener {
            if (mViewPager.currentItem == PagerAdapter.PAGE_END) {
                saveAndExit()
                return@setOnClickListener
            }

            changePage()
            updateProgressText()
        }

        updateButtonNames()
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgressText() {
        // Suppressing lint check for internationalizing text as we're only displaying numbers and
        // a forward slash - no words from any particular language.
        mProgressText.text =
                (mViewPager.currentItem + 1).toString() + " / " + PagerAdapter.PAGES_COUNT
    }

    /**
     * Changes the ViewPager's current item and handles validation for each page.
     *
     * @see hasMissingInputs
     * @see checkInvalidInputs
     * @see updateButtonNames
     */
    private fun changePage(goBack: Boolean = false) {
        mViewPager.currentItem = if (goBack) {
            mViewPager.currentItem - 1

        } else {
            if (hasMissingInputs()) {
                Snackbar.make(
                        findViewById(R.id.rootLayout),
                        R.string.welcome_inputs_missing,
                        Snackbar.LENGTH_SHORT
                ).show()
                return
            }

            if (!checkInvalidInputs()) {
                return
            }

            mViewPager.currentItem + 1
        }

        updateButtonNames()
    }

    /**
     * @return true if there is at least one missing input.
     */
    private fun hasMissingInputs(): Boolean {
        return when (mViewPager.currentItem) {
            PagerAdapter.PAGE_TIMETABLE_NAME -> !sTimetableBuilder.hasName()
            PagerAdapter.PAGE_TIMETABLE_DATES -> !sTimetableBuilder.hasDates()
            PagerAdapter.PAGE_TIMETABLE_SCHEDULING -> !sTimetableBuilder.hasWeekRotations()
            PagerAdapter.PAGE_SUBJECTS -> sSubjectText.isNullOrEmpty()
            else -> false
        }
    }

    /**
     * Checks the inputs for the current view pager item.
     * It will display Snackbar messages accordingly.
     *
     * @return whether there are any invalid inputs (i.e. true for okay inputs, false for invalid).
     */
    private fun checkInvalidInputs(): Boolean {
        when (mViewPager.currentItem) {
            PagerAdapter.PAGE_TIMETABLE_DATES -> {
                if (sTimetableBuilder.hasInvalidDates()) {
                    Snackbar.make(
                            findViewById(R.id.rootLayout),
                            R.string.message_start_date_after_end_date,
                            Snackbar.LENGTH_SHORT).show()
                    return false
                }
            }
        }

        return true
    }

    /**
     * Saves user's inputs to the database and exits to the main page.
     *
     * @see addTimetable
     * @see addSubjects
     */
    private fun saveAndExit() {
        val timetable = addTimetable()
        addSubjects(timetable.id)

        startActivity(Intent(this, MainActivity::class.java))
    }

    /**
     * Writes the new [Timetable] to the database.
     */
    private fun addTimetable(): Timetable {
        val dataHandler = TimetableHandler(this)

        val timetable = sTimetableBuilder.build(dataHandler.getHighestItemId() + 1)

        dataHandler.addItem(timetable)
        (application as TimetableApplication).setCurrentTimetable(this, timetable)

        return timetable
    }

    /**
     * Writes the user's [Subject]s to the database.
     * The subject names are derived from a string of names separated by line breaks, [sSubjectText].
     */
    private fun addSubjects(timetableId: Int) {
        val dataHandler = SubjectHandler(this)

        val subjectStrings = sSubjectText!!.split("\n")
        for (i in 1..subjectStrings.size) {
            val subjectStr = subjectStrings[i - 1]

            if (subjectStr.isNullOrEmpty()) {
                continue
            }

            // The color of the subject will be the index i, but adjusted if i is over 19
            val defaultColorId = (i + 19) % 19

            val subject = Subject(
                    dataHandler.getHighestItemId() + 1,
                    timetableId,
                    subjectStr,
                    "",
                    defaultColorId)
            dataHandler.addItem(subject)
        }
    }

    /**
     * Changes the names of the buttons from 'Next' to 'Finish' where applicable.
     */
    private fun updateButtonNames() {
        val currentItem = mViewPager.currentItem

        mNextButton.setText(if (currentItem == PagerAdapter.PAGE_END) {
            R.string.finish
        } else {
            R.string.next
        })

        mPrevButton.isEnabled = currentItem != PagerAdapter.PAGE_TIMETABLE_NAME
        mPrevButton.text = if (currentItem == PagerAdapter.PAGE_TIMETABLE_NAME) {
            ""
        } else {
            getString(R.string.back)
        }
    }

    /**
     * A simple class consisting of properties used to create a [Timetable] object.
     */
    private class TimetableBuilder {

        var name: String? = null
        var startDate: LocalDate? = null
        var endDate: LocalDate? = null
        var weekRotations: Int? = null

        fun hasName() = !name.isNullOrEmpty()

        fun hasDates() = startDate != null && endDate != null

        fun hasInvalidDates() = startDate!!.isAfter(endDate!!)

        fun hasWeekRotations() = weekRotations != null

        /**
         * @return a [Timetable] instance from the builder properties
         * @throws IllegalArgumentException if any of the properties is null (from [checkNotNull])
         */
        fun build(id: Int) = Timetable(
                id,
                checkNotNull(name),
                checkNotNull(startDate),
                checkNotNull(endDate),
                checkNotNull(weekRotations!!))
    }

    private class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        companion object {

            /**
             * The number of pages this adapter will handle.
             */
            const val PAGES_COUNT = 5

            const val PAGE_TIMETABLE_NAME = 0
            const val PAGE_TIMETABLE_DATES = 1
            const val PAGE_TIMETABLE_SCHEDULING = 2
            const val PAGE_SUBJECTS = 3
            const val PAGE_END = 4
        }

        override fun getCount() = PAGES_COUNT

        override fun getItem(position: Int): Fragment? {
            when (position) {
                PAGE_TIMETABLE_NAME -> return TimetableNameFragment()
                PAGE_TIMETABLE_DATES -> return TimetableDatesFragment()
                PAGE_TIMETABLE_SCHEDULING -> return TimetableSchedulingFragment()
                PAGE_SUBJECTS -> return SubjectsFragment()
                PAGE_END -> return EndFragment()
            }
            return null
        }
    }

    /**
     * A page for the user to enter the name of their new timetable.
     */
    class TimetableNameFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_welcome_timetable, container, false)

            val editText = rootView.findViewById(R.id.editText_name) as EditText
            editText.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int,
                                               after: Int) {}

                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    sTimetableBuilder.name = s.toString()
                }
            })

            return rootView
        }
    }

    /**
     * The portion of the UI for the user to set start and end dates of their new timetable.
     */
    class TimetableDatesFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(
                    R.layout.fragment_welcome_timetable_dates, container, false)

            setupDateTexts(rootView)

            return rootView
        }

        private fun setupDateTexts(rootView: View) {
            val formatter = DateTimeFormatter.ofPattern("dd MMMM uuuu")

            // Note -1s and +1s below because Android month values are from 0-11 (to correspond with
            // java.util.Calendar) but LocalDate month values are from 1-12

            val startDateText = rootView.findViewById(R.id.textView_start_date) as TextView
            startDateText.setOnClickListener {
                val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val newStartDate = LocalDate.of(year, month + 1, dayOfMonth)
                    startDateText.setTextColor(R.color.mdu_text_black)
                    startDateText.text = newStartDate.format(formatter)
                    sTimetableBuilder.startDate = newStartDate
                }

                val initialDate = sTimetableBuilder.startDate ?: LocalDate.now()

                DatePickerDialog(
                        activity,
                        dateSetListener,
                        initialDate.year,
                        initialDate.monthValue - 1,
                        initialDate.dayOfMonth).show()
            }

            val endDateText = rootView.findViewById(R.id.textView_end_date) as TextView
            endDateText.setOnClickListener {
                val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val newEndDate = LocalDate.of(year, month + 1, dayOfMonth)
                    endDateText.setTextColor(R.color.mdu_text_black)
                    endDateText.text = newEndDate.format(formatter)
                    sTimetableBuilder.endDate = newEndDate
                }

                val initialDate = sTimetableBuilder.endDate ?: LocalDate.now().plusMonths(8)

                DatePickerDialog(
                        activity,
                        dateSetListener,
                        initialDate.year,
                        initialDate.monthValue - 1,
                        initialDate.dayOfMonth).show()
            }
        }
    }

    /**
     * A page to allow the user to specify their timetable scheduling type.
     */
    class TimetableSchedulingFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(
                    R.layout.fragment_welcome_timetable_sched, container, false)

            setupLayout(rootView)

            return rootView
        }

        private fun setupLayout(rootView: View) {
            setupRadioButtons(rootView)

            val checkboxUseNumbers =
                    rootView.findViewById(R.id.checkbox_sched_use_numbers) as CheckBox
            checkboxUseNumbers.setOnCheckedChangeListener { _, isChecked ->
                PrefUtils.setWeekRotationShownWithNumbers(activity, isChecked)
            }
        }

        private fun setupRadioButtons(rootView: View) {
            with(rootView.findViewById(R.id.radio_scheduling_fixed) as RadioButton) {
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) sTimetableBuilder.weekRotations = 1
                }

                isChecked = true
            }

            @IdRes val otherRadioIds = arrayOf(
                    R.id.radio_scheduling_2,
                    R.id.radio_scheduling_3,
                    R.id.radio_scheduling_4)

            otherRadioIds.forEachIndexed { index, radioId ->
                (rootView.findViewById(radioId) as RadioButton)
                        .setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) sTimetableBuilder.weekRotations = index + 2
                        }
            }
        }
    }

    /**
     * On this page, the user can set terms/semesters and their dates.
     */
    class SubjectsFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_welcome_subjects, container, false)

            val editText = rootView.findViewById(R.id.editText) as EditText
            editText.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int,
                                               after: Int) {}

                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    sSubjectText = s.toString()
                }
            })

            return rootView
        }
    }

    /**
     * The page that displays a message to inform the user the timetable setup is complete.
     */
    class EndFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return inflater!!.inflate(R.layout.fragment_welcome_end, container, false)
        }
    }

}
