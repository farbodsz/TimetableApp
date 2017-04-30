package co.timetableapp.ui.schedule

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
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
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.ClassTimesSchema
import co.timetableapp.model.Class
import co.timetableapp.model.ClassDetail
import co.timetableapp.model.ClassTime
import co.timetableapp.model.Timetable
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.NavigationDrawerActivity
import co.timetableapp.ui.classes.ClassDetailActivity
import co.timetableapp.ui.components.DynamicPagerAdapter
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

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
        setupLayout()
    }

    private fun setupToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
    }

    private fun setupLayout() {
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

        val currentTimetable = (application as TimetableApplication).currentTimetable!!

        if (!currentTimetable.isValidToday()) {
            showEmptySchedulePlaceholder()
            return
        }

        val today = LocalDate.now()
        val todayTabIndex = getTodayTabIndex()

        var daysCount = 0

        for (weekNumber in 1..currentTimetable.weekRotations) {
            for (dayOfWeek in DayOfWeek.values()) {

                val thisDay = getTabDate(today, todayTabIndex, daysCount)
                Log.v(LOG_TAG, "Finding lessons for " + thisDay.toString())

                val tabTitle = makeTabName(dayOfWeek, currentTimetable, weekNumber)

                val classTimes =
                        getClassTimesForDay(currentTimetable, dayOfWeek, weekNumber, thisDay)

                if (classTimes.isEmpty()) {
                    // Show a placeholder if there aren't any classes to display for this day
                    val placeholder = UiUtils.makePlaceholderView(
                            this,
                            R.drawable.ic_today_black_24dp,
                            R.string.no_classes_today)
                    mPagerAdapter!!.addViewWithTitle(placeholder, title = tabTitle)
                    daysCount++
                    continue
                }

                classTimes.sort()

                val scheduleAdapter = ScheduleAdapter(this, classTimes)
                scheduleAdapter.setOnEntryClickListener { view, position ->
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
                with(recyclerView) {
                    layoutManager = LinearLayoutManager(context)
                    setHasFixedSize(true)
                    adapter = scheduleAdapter
                }

                mPagerAdapter!!.addViewWithTitle(recyclerView, title = tabTitle)

                daysCount++
            }
        }
    }

    /**
     * Displays a placeholder view for when the schedule cannot be displayed. This placeholder is
     * for the whole layout, not just one tab.
     */
    private fun showEmptySchedulePlaceholder() {
        val placeholder = UiUtils.makePlaceholderView(
                this,
                R.drawable.ic_today_black_24dp,
                R.string.no_classes_today)

        mPagerAdapter!!.addViewWithTitle(
                placeholder,
                title = getString(R.string.title_activity_schedule))
    }

    /**
     * @return an integer representing the list index of the tab for today. This number depends on
     * the day and week (rotation) of today.
     */
    private fun getTodayTabIndex(): Int {
        val today = LocalDate.now().dayOfWeek
        val nthWeek = DateUtils.findWeekNumber(application)
        return today.value + (nthWeek - 1) * 7 - 1
    }

    /**
     * Calculates a date for a tab in the schedule.
     *
     * @param today         the [LocalDate] for the current day
     * @param todayTabIndex the integer representing the list index of the tab for the current day
     * @param daysCount     the number of days (tabs) already added to the schedule UI
     *
     * @return the [LocalDate] for a date shown in the schedule
     */
    private fun getTabDate(today: LocalDate, todayTabIndex: Int, daysCount: Int): LocalDate {
        return if (todayTabIndex > daysCount) {
            // This day is before today
            today.minusDays((todayTabIndex - daysCount).toLong())

        } else if (todayTabIndex == daysCount) {
            // This day is today
            today

        } else {
            // This day is after today
            today.plusDays((daysCount - todayTabIndex).toLong())
        }
    }

    private fun makeTabName(dayOfWeek: DayOfWeek, timetable: Timetable, weekNumber: Int): String {
        val titleBuilder = StringBuilder()
                .append(dayOfWeek.toString())

        if (!timetable.hasFixedScheduling()) {
            titleBuilder.append(" ")
                    .append(ClassTime.getWeekText(this, weekNumber, false))
        }

        return titleBuilder.toString()
    }

    /**
     * @return a list of [ClassTime]s for a particular day
     */
    fun getClassTimesForDay(currentTimetable: Timetable,
                            dayOfWeek: DayOfWeek,
                            weekNumber: Int,
                            date: LocalDate): ArrayList<ClassTime> {
        val timetableId = currentTimetable.id

        val query = Query.Builder()
                .addFilter(Filters.equal(ClassTimesSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .addFilter(Filters.equal(ClassTimesSchema.COL_DAY, dayOfWeek.value.toString()))
                .addFilter(Filters.equal(ClassTimesSchema.COL_WEEK_NUMBER, weekNumber.toString()))
                .build()

        val classTimes = ArrayList<ClassTime>()

        ClassTimeHandler(this).getAllItems(query).forEach {
            val classDetail = ClassDetail.create(this, it.classDetailId)!!
            val cls = Class.create(this, classDetail.classId)!!

            if (cls.isCurrent(date)) {
                classTimes.add(it)
            }
        }

        return classTimes
    }

    private fun goToNow() {
        mViewPager!!.currentItem = getTodayTabIndex()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                setupTabContent()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_schedule, menu)
        UiUtils.tintMenuIcons(this, menu!!, R.id.action_today)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_today -> goToNow()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun getSelfNavDrawerItem() = NAVDRAWER_ITEM_SCHEDULE

    override fun getSelfToolbar() = findViewById(R.id.toolbar) as Toolbar

    override fun getSelfDrawerLayout() = findViewById(R.id.drawerLayout) as DrawerLayout

    override fun getSelfNavigationView() = findViewById(R.id.navigationView) as NavigationView

}
