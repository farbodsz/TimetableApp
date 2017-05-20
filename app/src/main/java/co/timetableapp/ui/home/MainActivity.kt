package co.timetableapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import co.timetableapp.R
import co.timetableapp.ui.assignments.AssignmentDetailActivity
import co.timetableapp.ui.base.NavigationDrawerActivity
import co.timetableapp.ui.events.EventDetailActivity
import co.timetableapp.ui.exams.ExamDetailActivity
import com.github.clans.fab.FloatingActionButton

/**
 * The main screen showing an overview of the user's classes, assignments and exams.
 *
 * The UI is divided into two pages: one for today, and another for upcoming events. The user can
 * navigate between the pages using tabs.
 *
 * @see TodayFragment
 * @see UpcomingFragment
 */
class MainActivity : NavigationDrawerActivity() {

    companion object {
        internal const val REQUEST_CODE_ITEM_DETAIL = 1
    }

    private val mViewPager by lazy { findViewById(R.id.viewPager) as ViewPager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        setupLayout()
    }

    private fun setupLayout() {
        setupFabMenu()

        mViewPager.adapter = PagerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabLayout) as TabLayout
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white))

        mViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(mViewPager)
    }

    private fun setupFabMenu() {
        val fabAssignment = findViewById(R.id.fab_assignment) as FloatingActionButton
        fabAssignment.setOnClickListener {
            val intent = Intent(this, AssignmentDetailActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL)
        }

        val fabExam = findViewById(R.id.fab_exam) as FloatingActionButton
        fabExam.setOnClickListener {
            val intent = Intent(this, ExamDetailActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL)
        }

        val fabEvent = findViewById(R.id.fab_event) as FloatingActionButton
        fabEvent.setOnClickListener {
            val intent = Intent(this, EventDetailActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ITEM_DETAIL)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        supportFragmentManager.fragments.forEach {
            // To update content in each fragment
            it.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_HOME

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

    private inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> TodayFragment()
                1 -> UpcomingFragment()
                else -> throw IllegalArgumentException("invalid position: $position")
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return getString(when (position) {
                0 -> R.string.tab_today
                1 -> R.string.tab_upcoming
                else -> throw IllegalArgumentException("invalid position: $position")
            }).toUpperCase()  // since we are using a style where textAllCaps="false"
        }

    }

}
