package com.satsumasoftware.timetable.ui.start

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
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
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.handler.TimetableHandler
import com.satsumasoftware.timetable.framework.Timetable
import com.satsumasoftware.timetable.ui.MainActivity
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * This activity guides the user through setting up their new timetable.
 */
class InitialSetupActivity : AppCompatActivity() {

    private var mViewPager: ViewPager? = null
    private var mProgressText: TextView? = null

    private var mPrevButton: Button? = null
    private var mNextButton: Button? = null

    private companion object{
        var sName: String? = null
        var sStartDate: LocalDate? = null
        var sEndDate: LocalDate? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)

        setupLayout()
    }

    private fun setupLayout() {
        mViewPager = findViewById(R.id.viewPager) as ViewPager
        mViewPager!!.adapter = PagerAdapter(supportFragmentManager)

        mProgressText = findViewById(R.id.textView_progress) as TextView
        updateProgressText()

        mPrevButton = findViewById(R.id.button_previous) as Button
        mPrevButton!!.setOnClickListener {
            changePage(true)
            updateProgressText()
        }

        mNextButton = findViewById(R.id.button_next) as Button
        mNextButton!!.setOnClickListener {
            if (mViewPager!!.currentItem == PagerAdapter.PAGE_END) {
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
        mProgressText!!.text =
                (mViewPager!!.currentItem + 1).toString() + " / " + PagerAdapter.PAGES_COUNT
    }

    /**
     * Changes the ViewPager's current item and handles validation for each page.
     *
     * @see hasMissingInputs
     * @see checkInvalidInputs
     * @see updateButtonNames
     */
    private fun changePage(goBack: Boolean = false) {
        mViewPager!!.currentItem = if (goBack) {
            mViewPager!!.currentItem - 1

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

            mViewPager!!.currentItem + 1
        }

        updateButtonNames()
    }

    /**
     * @return true if there is at least one missing input.
     */
    private fun hasMissingInputs(): Boolean {
        return when (mViewPager!!.currentItem) {
            PagerAdapter.PAGE_TIMETABLE_NAME -> sName.isNullOrEmpty()
            PagerAdapter.PAGE_TIMETABLE_DETAILS -> sStartDate == null || sEndDate == null
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
        when (mViewPager!!.currentItem) {
            PagerAdapter.PAGE_TIMETABLE_DETAILS -> {
                if (sStartDate!!.isAfter(sEndDate!!)) {
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
     * Saves the timetable to the database and exits to the main page.
     */
    private fun saveAndExit() {
        val dataHandler = TimetableHandler(this)

        val timetable = Timetable(
                dataHandler.getHighestItemId() + 1,
                checkNotNull(sName),
                checkNotNull(sStartDate),
                checkNotNull(sEndDate),
                1)  // default week rotations is 1

        dataHandler.addItem(timetable)

        (application as TimetableApplication).setCurrentTimetable(this, timetable)

        startActivity(Intent(this, MainActivity::class.java))
    }

    /**
     * Changes the names of the buttons from 'Next' to 'Finish' where applicable.
     */
    private fun updateButtonNames() {
        val currentItem = mViewPager!!.currentItem

        mNextButton!!.setText(if (currentItem == PagerAdapter.PAGE_END) {
            R.string.finish
        } else {
            R.string.next
        })

        mPrevButton!!.isEnabled = currentItem != PagerAdapter.PAGE_START
        mPrevButton!!.text = if (currentItem == PagerAdapter.PAGE_START) {
            ""
        } else {
            getString(R.string.back)
        }
    }

    private class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        companion object {

            /**
             * The number of pages this adapter will handle.
             */
            const val PAGES_COUNT = 4

            const val PAGE_START = 0
            const val PAGE_TIMETABLE_NAME = 1
            const val PAGE_TIMETABLE_DETAILS = 2
            const val PAGE_END = 3
        }

        override fun getCount() = PAGES_COUNT

        override fun getItem(position: Int): Fragment? {
            when (position) {
                PAGE_START -> return StartFragment()
                PAGE_TIMETABLE_NAME -> return TimetableNameFragment()
                PAGE_TIMETABLE_DETAILS -> return TimetableDetailsFragment()
                PAGE_END -> return EndFragment()
            }
            return null
        }
    }

    /**
     * A part of the UI that displays a welcome/start message to the user.
     */
    class StartFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return inflater!!.inflate(R.layout.fragment_welcome_start, container, false)
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
                    sName = s.toString()
                }
            })

            return rootView
        }
    }

    /**
     * The portion of the UI for the user to set start and end dates of their new timetable.
     */
    class TimetableDetailsFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(
                    R.layout.fragment_welcome_timetable_details, container, false)

            setupDateTexts(rootView)

            return rootView
        }

        private fun setupDateTexts(rootView: View) {
            val formatter = DateTimeFormatter.ofPattern("dd MMMM uuuu")

            val startDateText = rootView.findViewById(R.id.textView_start_date) as TextView
            startDateText.setOnClickListener {
                // Note -1s and +1s because Android month values are from 0-11 (to correspond with
                // java.util.Calendar) but LocalDate month values are from 1-12

                val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    sStartDate = LocalDate.of(year, month + 1, dayOfMonth)

                    startDateText.setTextColor(R.color.mdu_text_black)
                    startDateText.text = sStartDate!!.format(formatter)
                }

                val initialDate = sStartDate ?: LocalDate.now()

                DatePickerDialog(activity,
                        dateSetListener,
                        initialDate.year,
                        initialDate.monthValue - 1,
                        initialDate.dayOfMonth).show()
            }

            val endDateText = rootView.findViewById(R.id.textView_end_date) as TextView
            endDateText.setOnClickListener {
                val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    sEndDate = LocalDate.of(year, month + 1, dayOfMonth)

                    endDateText.setTextColor(R.color.mdu_text_black)
                    endDateText.text = sEndDate!!.format(formatter)
                }

                val initialDate = sEndDate ?: LocalDate.now().plusMonths(8)

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
     * The page that displays a message to inform the user the timetable setup is complete.
     */
    class EndFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return inflater!!.inflate(R.layout.fragment_welcome_end, container, false)
        }
    }

}
