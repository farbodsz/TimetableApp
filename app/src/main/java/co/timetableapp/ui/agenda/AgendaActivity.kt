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

package co.timetableapp.ui.agenda

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import co.timetableapp.R
import co.timetableapp.model.agenda.AgendaType
import co.timetableapp.ui.NewItemSelectorFragment
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.NavigationDrawerActivity
import co.timetableapp.ui.events.EventDetailActivity
import co.timetableapp.ui.exams.ExamDetailActivity
import co.timetableapp.util.PrefUtils
import java.util.*

/**
 * An activity for displaying the user's agenda - upcoming assignments, exams, etc.
 *
 * @see AgendaFragment
 */
class AgendaActivity : NavigationDrawerActivity() {

    companion object {

        const val REQUEST_CODE_CREATE_ITEM = 1

        const val DEFAULT_SHOW_PAST = false
    }

    private val mViewPager by lazy { findViewById<ViewPager>(R.id.viewPager) }

    private var mShowCompleted = true
    private var mShowPast = DEFAULT_SHOW_PAST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        mShowCompleted = PrefUtils.showCompletedAgendaItems(this)

        setupToolbar()
        setupLayout()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupLayout() {
        mViewPager.adapter = PagerAdapter(supportFragmentManager)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white))

        mViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(mViewPager)

        setupFab()
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val dialogFragment = NewItemSelectorFragment()

            dialogFragment.onCreateNewAgendaItem { _, dialog, agendaType ->
                val detailActivity = when (agendaType) {
                    AgendaType.ASSIGNMENT -> AssignmentDetailActivity::class.java
                    AgendaType.EXAM -> ExamDetailActivity::class.java
                    AgendaType.EVENT -> EventDetailActivity::class.java
                }

                val intent = Intent(this, detailActivity)
                startActivityForResult(intent, REQUEST_CODE_CREATE_ITEM)
                dialog.dismiss()
            }

            dialogFragment.show(supportFragmentManager, dialogFragment.tag)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CREATE_ITEM) {
            supportFragmentManager.fragments.forEach {
                // Pass on the result to the fragments so they can update themselves
                it.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_agenda, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilterDialog() {
        val oldShowCompleted = mShowCompleted

        val multiChoiceListener = DialogInterface.OnMultiChoiceClickListener { _, which, isChecked ->
            when (which) {
                0 -> mShowCompleted = isChecked
                1 -> mShowPast = isChecked
                else -> throw UnsupportedOperationException("expected position: 0")
            }
        }

        AlertDialog.Builder(this)
                .setTitle(R.string.action_filter)
                .setMultiChoiceItems(
                        R.array.filter_agenda_options,
                        booleanArrayOf(mShowCompleted, mShowPast),
                        multiChoiceListener)
                .setPositiveButton(R.string.action_filter, { _, _ ->
                    supportFragmentManager.fragments.forEach {
                        (it as OnFilterChangeListener).onFilterChange(mShowCompleted, mShowPast)
                    }

                    if (mShowCompleted != oldShowCompleted) {
                        val stringRes = if (mShowCompleted)
                            R.string.agenda_showing_completed
                        else
                            R.string.agenda_hiding_completed

                        val snackbar = Snackbar.make(
                                findViewById(R.id.coordinatorLayout),
                                stringRes,
                                Snackbar.LENGTH_LONG)

                        snackbar.setAction(R.string.always_do_this, {
                            PrefUtils.setShowCompletedAgendaItems(this, mShowCompleted)
                        })

                        snackbar.show()
                    }
                })
                .show()
    }

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_AGENDA

    override fun getSelfToolbar(): Toolbar = findViewById(R.id.toolbar)

    override fun getSelfDrawerLayout(): DrawerLayout = findViewById(R.id.drawerLayout)

    override fun getSelfNavigationView(): NavigationView = findViewById(R.id.navigationView)

    /**
     * Interface definition for a callback to be invoked when the agenda filter has been updated.
     *
     * This should be implemented in fragments to define what happens when the filter changes.
     */
    interface OnFilterChangeListener {

        /**
         * Callback method to be invoked when the agenda filter has been updated.
         */
        fun onFilterChange(showCompleted: Boolean, showPast: Boolean)

    }

    private inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = 3

        override fun getItem(position: Int): Fragment {
            val fragment = when (position) {
                0, 1, 2 -> AgendaFragment()
                else -> throw IllegalArgumentException("invalid position: $position")
            }

            val itemTypeArg = when (position) {
                0 -> EnumSet.of(AgendaType.ASSIGNMENT)
                1 -> EnumSet.of(AgendaType.EXAM)
                2 -> EnumSet.of(AgendaType.EVENT)
                else -> throw IllegalArgumentException("invalid position: $position")
            }

            val args = Bundle()
            args.putSerializable(AgendaFragment.ARGUMENT_LIST_TYPE, itemTypeArg)

            fragment.arguments = args

            return fragment
        }

        override fun getPageTitle(position: Int): CharSequence {
            val drawableRes = when (position) {
                0 -> R.drawable.ic_homework_white_24dp
                1 -> R.drawable.ic_assessment_white_24dp
                2 -> R.drawable.ic_event_white_24dp
                else -> throw IllegalArgumentException("invalid position: $position")
            }

            val drawable = ContextCompat.getDrawable(this@AgendaActivity, drawableRes)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

            val spannableString = SpannableString(" ")
            val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannableString
        }

    }

}
