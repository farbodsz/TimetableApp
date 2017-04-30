package co.timetableapp.ui.schedule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.model.Class
import co.timetableapp.model.ClassDetail
import co.timetableapp.model.ClassTime
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.NavigationDrawerActivity
import co.timetableapp.ui.classes.ClassDetailActivity
import co.timetableapp.ui.components.DynamicPagerAdapter
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import java.util.*

/**
 * An activity for displaying a schedule for a particular week.
 *
 * Each tab represents a day of the week, displaying a list of classes for that day.
 */
class ScheduleActivity : NavigationDrawerActivity() {

    companion object {

        private const val LOG_TAG = "ScheduleActivity"

        private const val REQUEST_CODE_CLASS_DETAIL = 1
    }

    private var mPagerAdapter: DynamicPagerAdapter? = null
    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        setupToolbar()
        setupTabs()
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    private fun setupTabs() {
        val tabLayout = findViewById(R.id.tabLayout) as TabLayout
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE

        mPagerAdapter = DynamicPagerAdapter()

        mViewPager = findViewById(R.id.viewPager) as ViewPager
        mViewPager!!.adapter = mPagerAdapter

        setupTabContent()

        mViewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white))
        tabLayout.setupWithViewPager(mViewPager)
    }

    private fun setupTabContent() {
        mPagerAdapter!!.removeAllViews(mViewPager!!)

        val timetable = (application as TimetableApplication).currentTimetable!!

        if (!timetable.isValidToday()) {
            val placeholder = UiUtils.makePlaceholderView(
                    this,
                    R.drawable.ic_today_black_24dp,
                    R.string.no_classes_today)
            mPagerAdapter!!.addViewWithTitle(
                    placeholder,
                    title = getString(R.string.title_activity_schedule))
            return
        }

        val today = LocalDate.now()
        val indexOfToday = getIndexOfTodayTab()

        var daysCount = 0

        for (weekNumber in 1..timetable.weekRotations) {
            for (dayOfWeek in DayOfWeek.values()) {

                val thisDay: LocalDate
                if (indexOfToday > daysCount) {
                    // This day is before today
                    thisDay = today.minusDays((indexOfToday - daysCount).toLong())

                } else if (indexOfToday == daysCount) {
                    // This day is today
                    thisDay = today

                } else {
                    // This day is after today
                    thisDay = today.plusDays((daysCount - indexOfToday).toLong())
                }
                Log.i(LOG_TAG, "Finding lessons for " + thisDay.toString())

                val titleBuilder = StringBuilder()
                titleBuilder.append(dayOfWeek.toString())
                if (!timetable.hasFixedScheduling()) {
                    titleBuilder.append(" ")
                            .append(ClassTime.getWeekText(this, weekNumber, false))
                }
                val tabTitle = titleBuilder.toString()

                val classTimes = ClassTimeHandler.getClassTimesForDay(this, dayOfWeek, weekNumber, thisDay)

                if (classTimes.isEmpty()) {
                    val placeholder = UiUtils.makePlaceholderView(
                            this,
                            R.drawable.ic_today_black_24dp,
                            R.string.no_classes_today)
                    mPagerAdapter!!.addViewWithTitle(placeholder, title = tabTitle)
                    daysCount++
                    continue
                }

                Collections.sort(classTimes)

                val adapter = ScheduleAdapter(this, classTimes)
                adapter.setOnEntryClickListener { view, position ->
                    val classTime = classTimes[position]
                    val classDetail = ClassDetail.create(baseContext, classTime.classDetailId)

                    val cls = Class.create(this, classDetail!!.id)

                    val intent = Intent(this, ClassDetailActivity::class.java)
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM, cls)

                    var bundle: Bundle? = null
                    if (UiUtils.isApi21()) {
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                this,
                                view,
                                getString(R.string.transition_1))
                        bundle = options.toBundle()
                    }

                    ActivityCompat.startActivityForResult(this, intent, REQUEST_CODE_CLASS_DETAIL, bundle)
                }

                val recyclerView = RecyclerView(this)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = adapter

                mPagerAdapter!!.addViewWithTitle(recyclerView, title = tabTitle)

                daysCount++
            }
        }
    }

    private fun getIndexOfTodayTab(): Int {
        val today = LocalDate.now().dayOfWeek
        val nthWeek = DateUtils.findWeekNumber(application)
        return today.value + (nthWeek - 1) * 7 - 1
    }

    private fun goToNow() {
        mViewPager!!.currentItem = getIndexOfTodayTab()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                setupTabContent()
                goToNow()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_schedule, menu)
        UiUtils.tintMenuIcons(this, menu!!, R.id.action_today, R.id.action_view_week)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_today -> goToNow()
            R.id.action_view_week -> {
                Snackbar.make(
                        findViewById(R.id.drawerLayout),
                        "Week view coming soon",
                        Snackbar.LENGTH_SHORT).show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_SCHEDULE

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

}
