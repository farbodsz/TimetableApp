package com.satsumasoftware.timetable.ui.start

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.satsumasoftware.timetable.R

class WelcomeActivity : AppCompatActivity() {

    private var mViewPager: ViewPager? = null
    private var mProgressText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setupLayout()
    }

    private fun setupLayout() {
        setupTabs()

        mProgressText = findViewById(R.id.textView_progress) as TextView
        updateProgressText()

        val prevButton = findViewById(R.id.button_previous) as Button
        prevButton.setOnClickListener {
            mViewPager!!.currentItem--
            updateProgressText()
        }

        val nextButton = findViewById(R.id.button_next) as Button
        nextButton.setOnClickListener {
            mViewPager!!.currentItem++
            updateProgressText()

        }
    }

    private fun updateProgressText() {
        mProgressText!!.text = mViewPager!!.currentItem.toString() + " / 5"
    }

    private fun setupTabs() {
        mViewPager = findViewById(R.id.viewPager) as ViewPager

        with(mViewPager!!) {
            adapter = ViewPagerAdapter(supportFragmentManager)
        }
    }

    private class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = 1

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return StartFragment()
            }
            return null
        }
    }

    class StartFragment: Fragment() {

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val rootView = inflater!!.inflate(R.layout.fragment_welcome_start, container, false)
            return rootView
        }
    }

}
