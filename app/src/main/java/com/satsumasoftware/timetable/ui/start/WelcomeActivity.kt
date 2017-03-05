package com.satsumasoftware.timetable.ui.start

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
import com.satsumasoftware.timetable.db.handler.TimetableHandler
import com.satsumasoftware.timetable.framework.Timetable
import org.threeten.bp.LocalDate

class WelcomeActivity : AppCompatActivity() {

    private var mViewPager: ViewPager? = null
    private var mProgressText: TextView? = null

    companion object{
        private var sName: String? = null
        private var sStartDate: LocalDate? = null
        private var sEndDate: LocalDate? = null
        private var sWeekRotations: Int? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setupLayout()
    }

    private fun setupLayout() {
        mViewPager = findViewById(R.id.viewPager) as ViewPager
        mViewPager!!.adapter = ViewPagerAdapter(supportFragmentManager)

        mProgressText = findViewById(R.id.textView_progress) as TextView
        updateProgressText()

        val prevButton = findViewById(R.id.button_previous) as Button
        prevButton.setOnClickListener {
            changePage(true)
            updateProgressText()
        }

        val nextButton = findViewById(R.id.button_next) as Button
        nextButton.setOnClickListener {
            changePage()
            updateProgressText()
        }
    }

    private fun updateProgressText() {
        mProgressText!!.text = (mViewPager!!.currentItem + 1).toString() + " / 5"
    }

    private fun changePage(goBack: Boolean = false) {
        mViewPager!!.currentItem = if (goBack) {
            mViewPager!!.currentItem - 1

        } else {
            if (mViewPager!!.currentItem == 1) {
                // Just changed name - validate this name
                if (sName.isNullOrEmpty()) {
                    Snackbar.make( findViewById(R.id.rootLayout),
                            R.string.welcome_timetable_missing,
                            Snackbar.LENGTH_SHORT).show()
                    return
                }
            }

            mViewPager!!.currentItem + 1
        }
    }

    fun createTimetable(): Timetable {
        return Timetable(
                TimetableHandler(this).getHighestItemId() + 1,
                checkNotNull(sName),
                checkNotNull(sStartDate),
                checkNotNull(sEndDate),
                checkNotNull(sWeekRotations))
    }

    private class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = 2

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return StartFragment()
                1 -> return TimetableNameFragment()
            }
            return null
        }
    }

    class StartFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            return inflater!!.inflate(R.layout.fragment_welcome_start, container, false)
        }
    }

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

}
