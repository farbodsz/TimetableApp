package co.timetableapp.ui.assignments

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
import co.timetableapp.ui.base.NavigationDrawerActivity
import co.timetableapp.ui.exams.ExamsFragment

/**
 * An activity for displaying the user's agenda - upcoming assignments, exams, etc.
 */
class AgendaActivity : NavigationDrawerActivity() {

    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        setupToolbar()
        setupLayout()
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    private fun setupLayout() {
        mViewPager = findViewById(R.id.viewPager) as ViewPager
        mViewPager!!.adapter = PagerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabLayout) as TabLayout
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white))

        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.setupWithViewPager(mViewPager)
    }

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_AGENDA

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

    private inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> AssignmentsFragment()
                1 -> ExamsFragment()
                else -> throw IllegalArgumentException("invalid position: $position")
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return getString(when (position) {
                0 -> R.string.title_activity_assignments
                1 -> R.string.title_activity_exams
                else -> throw IllegalArgumentException("invalid position: $position")
            })
        }

    }

}
