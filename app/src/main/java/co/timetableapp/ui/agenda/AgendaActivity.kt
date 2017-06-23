package co.timetableapp.ui.agenda

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
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
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.NavigationDrawerActivity
import co.timetableapp.ui.events.EventDetailActivity
import co.timetableapp.ui.exams.ExamDetailActivity
import co.timetableapp.util.PrefUtils
import com.github.clans.fab.FloatingActionMenu

/**
 * An activity for displaying the user's agenda - upcoming assignments, exams, etc.
 *
 * @see AssignmentsFragment
 * @see ExamsFragment
 * @see EventsFragment
 */
class AgendaActivity : NavigationDrawerActivity() {

    companion object {

        const val REQUEST_CODE_CREATE_ITEM = 1

        const val DEFAULT_SHOW_PAST = false
    }

    private val mViewPager by lazy { findViewById(R.id.viewPager) as ViewPager }

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
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    private fun setupLayout() {
        mViewPager.adapter = PagerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabLayout) as TabLayout
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white))

        mViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(mViewPager)

        setupFab()
    }

    private fun setupFab() {
        val fabIds = arrayOf(R.id.fab_assignment, R.id.fab_exam, R.id.fab_event)
        val detailActivities = arrayOf(
                AssignmentDetailActivity::class.java,
                ExamDetailActivity::class.java,
                EventDetailActivity::class.java)  // must correspond to list of FAB ids

        fabIds.forEachIndexed { index, id ->
            findViewById(id).setOnClickListener {
                val intent = Intent(this, detailActivities[index])
                startActivityForResult(intent, REQUEST_CODE_CREATE_ITEM)

                (findViewById(R.id.fabMenu) as FloatingActionMenu).close(false)
            }
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

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

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
            return when (position) {
                0 -> AssignmentsFragment()
                1 -> ExamsFragment()
                2 -> EventsFragment()
                else -> throw IllegalArgumentException("invalid position: $position")
            }
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
