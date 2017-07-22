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

package co.timetableapp.ui.classes

import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.handler.ClassDetailHandler
import co.timetableapp.data.handler.ClassHandler
import co.timetableapp.data.handler.ClassTimeHandler
import co.timetableapp.model.*
import co.timetableapp.ui.base.ItemEditActivity
import co.timetableapp.ui.components.DynamicPagerAdapter
import co.timetableapp.ui.components.SubjectSelectorHelper
import co.timetableapp.ui.subjects.SubjectEditActivity
import co.timetableapp.util.DateUtils
import co.timetableapp.util.UiUtils
import co.timetableapp.util.title
import org.threeten.bp.LocalDate

/**
 * An activity for the user to edit [classes][Class].
 *
 * @see ClassesActivity
 * @see ClassDetailActivity
 */
class ClassEditActivity : ItemEditActivity<Class>() {

    companion object {

        private const val LOG_TAG = "ClassDetailActivity"

        private const val REQUEST_CODE_SUBJECT_DETAIL = 2
        private const val REQUEST_CODE_CLASS_TIME_DETAIL = 3
    }

    private var mNewDetailIdCount = 1

    private val mClassHandler = ClassHandler(this)
    private val mClassDetailHandler = ClassDetailHandler(this)

    private val mClassDetailIds = ArrayList<Int>()

    private lateinit var mAppBarLayout: AppBarLayout
    private lateinit var mTabLayout: TabLayout

    private var mSubject: Subject? = null
    private lateinit var mSubjectHelper: SubjectSelectorHelper

    private lateinit var mModuleEditText: EditText

    private var mStartDate: LocalDate? = null
    private var mEndDate: LocalDate? = null
    private lateinit var mStartDateText: TextView
    private lateinit var mEndDateText: TextView

    private val mPagerAdapter = DynamicPagerAdapter()

    private val mAllClassTimeGroups = ArrayList<ArrayList<ClassTimeGroup>>()
    private val mAdapters = ArrayList<ClassTimesAdapter>()

    override fun getLayoutResource() = R.layout.activity_class_edit

    override fun getTitleRes(isNewItem: Boolean) = if (isNewItem) {
        R.string.title_activity_class_new
    } else {
        R.string.title_activity_class_edit
    }

    override fun setupLayout() {
        mAppBarLayout = findViewById(R.id.appBarLayout) as AppBarLayout

        mModuleEditText = findViewById(R.id.editText_module) as EditText
        if (!mIsNew) {
            mModuleEditText.setText(mItem!!.moduleName)
        }

        setupDateTexts()
        setupDateSwitch()

        setupExpandToggle()

        setupTabs()

        setupSubjectText()
    }

    private fun setupDateTexts() {
        mStartDateText = findViewById(R.id.textView_start_date) as TextView
        mEndDateText = findViewById(R.id.textView_end_date) as TextView

        if (!mIsNew && mItem!!.hasStartEndDates()) {
            mStartDate = mItem!!.startDate
            mEndDate = mItem!!.endDate
            updateDateTexts()
        }

        mStartDateText.setOnClickListener {
            // note: -1 and +1s in code because Android month values are from 0-11 (to
            // correspond with java.util.Calendar) but LocalDate month values are from 1-12

            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mStartDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateDateTexts()
            }

            val useNowTime = mIsNew || !mItem!!.hasStartEndDates()

            with(mStartDate!!) {
                DatePickerDialog(
                        this@ClassEditActivity,
                        listener,
                        if (useNowTime) LocalDate.now().year else year,
                        if (useNowTime) LocalDate.now().monthValue - 1 else monthValue - 1,
                        if (useNowTime) LocalDate.now().dayOfMonth else dayOfMonth
                ).show()
            }
        }

        mEndDateText.setOnClickListener {
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mEndDate = LocalDate.of(year, month + 1, dayOfMonth)
                updateDateTexts()
            }

            val useNowTime = mIsNew || !mItem!!.hasStartEndDates()

            with(mEndDate!!) {
                DatePickerDialog(
                        this@ClassEditActivity,
                        listener,
                        if (useNowTime) LocalDate.now().year else year,
                        if (useNowTime) LocalDate.now().monthValue - 1 else monthValue - 1,
                        if (useNowTime) LocalDate.now().dayOfMonth else dayOfMonth
                ).show()
            }
        }
    }

    private fun updateDateTexts() {
        val formatter = DateUtils.FORMATTER_FULL_DATE

        if (mStartDate != null) {
            mStartDateText.text = mStartDate!!.format(formatter)
            mStartDateText.setTextColor(ContextCompat.getColor(
                    baseContext, R.color.mdu_text_black))
        }
        if (mEndDate != null) {
            mEndDateText.text = mEndDate!!.format(formatter)
            mEndDateText.setTextColor(ContextCompat.getColor(
                    baseContext, R.color.mdu_text_black))
        }
    }

    private fun setupDateSwitch() {
        val datesSwitch = findViewById(R.id.dates_switch) as Switch
        val datesSection = findViewById(R.id.dates_section)

        datesSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                datesSection.visibility = View.VISIBLE
                if (!mIsNew && mItem!!.hasStartEndDates()) {
                    mStartDate = mItem!!.startDate
                    mEndDate = mItem!!.endDate
                } else {
                    val today = LocalDate.now()
                    mStartDate = today
                    mEndDate = today.plusMonths(1)
                }
                updateDateTexts()
            } else {
                datesSection.visibility = View.GONE
                mStartDate = null
                mEndDate = null
            }
        }

        if (!mIsNew && mItem!!.hasStartEndDates()) {
            datesSwitch.isChecked = true
        }
    }

    private fun setupExpandToggle() {
        val detailSection = findViewById(R.id.linearLayout_details)

        val expandToggle = findViewById(R.id.expand_toggle)
        val expandIcon = findViewById(R.id.expand_icon) as ImageView

        expandToggle.setOnClickListener(object : View.OnClickListener {
            internal var mIsExpanded = false

            override fun onClick(v: View) {
                val drawableResId: Int
                val sectionVisibility: Int

                if (mIsExpanded) {
                    // We should condense the detail section
                    drawableResId = R.drawable.ic_expand_more_black_24dp
                    sectionVisibility = View.GONE
                } else {
                    // We should expand the detail section
                    drawableResId = R.drawable.ic_expand_less_black_24dp
                    sectionVisibility = View.VISIBLE
                }

                detailSection.visibility = sectionVisibility
                expandIcon.setImageResource(drawableResId)

                mIsExpanded = !mIsExpanded
            }
        })
    }

    private fun setupSubjectText() {
        mSubjectHelper = SubjectSelectorHelper(this, R.id.textView_subject)

        mSubjectHelper.onNewSubjectListener = DialogInterface.OnClickListener { _, _ ->
            val intent = Intent(this, SubjectEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SUBJECT_DETAIL)
        }

        mSubjectHelper.onSubjectChangeListener = object : SubjectSelectorHelper.OnSubjectChangeListener {
            override fun onSubjectChange(subject: Subject?) {
                mSubject = subject!!
                val color = Color(mSubject!!.colorId)
                UiUtils.setBarColors(
                        color,
                        this@ClassEditActivity,
                        mAppBarLayout,
                        mToolbar!!,
                        mTabLayout)
            }
        }

        val subject = if (mIsNew) null else Subject.create(this, mItem!!.subjectId)
        mSubjectHelper.setup(subject)
    }

    /**
     * Initialises the TabLayout and sets it up with a ViewPager, before populating the tabs.
     *
     * @see populateTabs
     */
    private fun setupTabs() {
        mTabLayout = findViewById(R.id.tabLayout) as TabLayout
        mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE

        val viewPager = findViewById(R.id.viewPager) as ViewPager
        viewPager.adapter = mPagerAdapter

        mTabLayout.setupWithViewPager(viewPager)

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))

        mTabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.mdu_text_white_secondary),
                ContextCompat.getColor(this, R.color.mdu_text_white))

        populateTabs()
    }

    private fun populateTabs() {
        if (!mIsNew) {
            val classDetails = ClassDetailHandler.getClassDetailsForClass(this, mItem!!.id)
            for (classDetail in classDetails) {
                addDetailTab(classDetail, false)
            }
        } else {
            addDetailTab(null, false) // first tab for adding detail
        }

        addDetailTab(null, true) // final tab for adding more pages
    }

    private fun addDetailTab(classDetail: ClassDetail?, placeholder: Boolean) {
        val isNewDetail = classDetail == null

        val pagerCount = mPagerAdapter.count // doesn't change as adapter count updates

        val classDetailId =
                classDetail?.id ?: mClassDetailHandler.getHighestItemId() + mNewDetailIdCount
        mClassDetailIds.add(classDetailId)

        val page = layoutInflater.inflate(R.layout.fragment_class_edit, null)

        val room = page.findViewById(R.id.editText_room) as EditText
        if (!isNewDetail) {
            room.setText(classDetail!!.room)
        }

        val building = page.findViewById(R.id.editText_building) as EditText
        if (!isNewDetail) {
            building.setText(classDetail!!.building)
        }

        val teacher = page.findViewById(R.id.editText_teacher) as EditText
        if (!isNewDetail) {
            teacher.setText(classDetail!!.teacher)
        }

        val classTimes = if (isNewDetail) {
            ArrayList<ClassTime>()
        } else {
            ClassTimeHandler.getClassTimesForDetail(this, classDetail!!.id)
        }

        val classTimeGroups = sortAndGroupTimes(classTimes)
        mAllClassTimeGroups.add(classTimeGroups)

        val adapter = ClassTimesAdapter(this, classTimeGroups)
        adapter.onItemClick { view, position ->
            val classTimeGroup = classTimeGroups[position]

            val intent = Intent(this, ClassTimeEditActivity::class.java)
            with(intent) {
                putExtra(ClassTimeEditActivity.EXTRA_CLASS_TIME, classTimeGroup.classTimes)
                putExtra(ClassTimeEditActivity.EXTRA_CLASS_DETAIL_ID, classDetailId)
                putExtra(ClassTimeEditActivity.EXTRA_TAB_POSITION, pagerCount)
            }

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.transition_2))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(
                    this, intent, REQUEST_CODE_CLASS_TIME_DETAIL, bundle)
        }

        mAdapters.add(adapter)

        val recyclerView = page.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically() = false
        }
        recyclerView.adapter = adapter

        val btnAddTime = page.findViewById(R.id.button_add_time) as Button
        btnAddTime.setOnClickListener { view ->
            val intent = Intent(this, ClassTimeEditActivity::class.java)
            intent.putExtra(ClassTimeEditActivity.EXTRA_CLASS_DETAIL_ID, classDetailId)
            intent.putExtra(ClassTimeEditActivity.EXTRA_TAB_POSITION, pagerCount)

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        view,
                        getString(R.string.transition_2))
                bundle = options.toBundle()
            }

            ActivityCompat.startActivityForResult(
                    this, intent, REQUEST_CODE_CLASS_TIME_DETAIL, bundle)
        }

        if (placeholder) {
            val content = page.findViewById(R.id.content) as ViewGroup
            content.visibility = View.GONE

            val btnAddTab = page.findViewById(R.id.button_add_tab) as Button
            btnAddTab.visibility = View.VISIBLE
            btnAddTab.setOnClickListener {
                content.visibility = View.VISIBLE
                btnAddTab.visibility = View.GONE
                addDetailTab(null, true)
            }
        }

        mPagerAdapter.addViewWithTitle(page, title="Detail ${pagerCount + 1}")

        if (isNewDetail) {
            mNewDetailIdCount++
        }
    }

    private fun sortAndGroupTimes(classTimes: ArrayList<ClassTime>): ArrayList<ClassTimeGroup> {
        if (classTimes.isEmpty()) {
            return ArrayList()
        }

        classTimes.sortWith(ClassTime.TimeComparator())

        val classTimeGroups = ArrayList<ClassTimeGroup>()
        var currentGroup: ClassTimeGroup? = null

        for (classTime in classTimes) {
            if (currentGroup == null) {
                currentGroup = ClassTimeGroup(classTime.startTime, classTime.endTime)
                currentGroup.addClassTime(classTime)
                continue
            }

            if (currentGroup.canAdd(classTime)) {
                currentGroup.addClassTime(classTime)
            } else {
                classTimeGroups.add(currentGroup) // add what has been produced up to this point
                currentGroup = ClassTimeGroup(classTime.startTime, classTime.endTime)
                currentGroup.addClassTime(classTime)
            }
        }

        currentGroup?.let {
            classTimeGroups.add(it) // add what was made in the last round of iterations
        }

        return classTimeGroups
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_SUBJECT_DETAIL -> if (resultCode == Activity.RESULT_OK) {
                mSubject = data!!.getParcelableExtra(ItemEditActivity.EXTRA_ITEM)
                mSubjectHelper.updateSubject(mSubject)
            }

            REQUEST_CODE_CLASS_TIME_DETAIL -> if (resultCode == Activity.RESULT_OK) {
                val tabIndex = data!!.getIntExtra(ClassTimeEditActivity.EXTRA_TAB_POSITION, -1)

                Log.i(LOG_TAG, "Reloading class times list for tab index $tabIndex")

                val thisTabTimeGroups = mAllClassTimeGroups[tabIndex]

                val classTimes =
                        ClassTimeHandler.getClassTimesForDetail(this, mClassDetailIds[tabIndex])

                thisTabTimeGroups.clear()
                thisTabTimeGroups.addAll(sortAndGroupTimes(classTimes))

                mAdapters[tabIndex].notifyDataSetChanged()
            }
        }
    }

    override fun handleDoneAction() {
        // Validate subject and start/end dates

        if (mSubject == null) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_subject_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if (mStartDate == null && mEndDate != null || mStartDate != null && mEndDate == null) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_both_dates_required,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if (mStartDate != null && mStartDate!!.isAfter(mEndDate)) {
            Snackbar.make(
                    findViewById(R.id.rootView),
                    R.string.message_start_date_after_end_date,
                    Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val moduleName = mModuleEditText.text.toString().title()

        // Go through each page and only collect data first - so any validation errors can be
        // resolved without any data being written or saved

        val pages = mPagerAdapter.getAllViews()

        val classDetailIds = ArrayList<Int>()
        val rooms = ArrayList<String>()
        val buildings = ArrayList<String>()
        val teachers = ArrayList<String>()

        // Note the second -1 from .size() to exclude the placeholder tab
        for (i in 0..(pages.size-1)-1) {
            Log.v(LOG_TAG, "Collecting values at tab index $i")

            val page = pages[i]

            val classDetailId = mClassDetailIds[i]

            val roomText = page.findViewById(R.id.editText_room) as EditText
            val room = roomText.text.toString().title()

            val buildingText = page.findViewById(R.id.editText_building) as EditText
            val building = buildingText.text.toString().title()

            val teacherText = page.findViewById(R.id.editText_teacher) as EditText
            val teacher = teacherText.text.toString().title()

            val classTimeGroups = mAllClassTimeGroups[i]
            if (classTimeGroups.isEmpty()) {
                Log.v(LOG_TAG, "Class times list is empty!")

                if (room.trim({ it <= ' ' }) == "" && teacher.trim({ it <= ' ' }) == "") {
                    Log.v(LOG_TAG, "Completely empty detail page")
                    Snackbar.make(
                            findViewById(R.id.rootView),
                            R.string.message_empty_detail,
                            Snackbar.LENGTH_SHORT
                    ).show()

                } else {
                    Log.v(LOG_TAG, "Room and/or teacher entered, but not times")
                    Snackbar.make(
                            findViewById(R.id.rootView),
                            R.string.message_missing_time_for_detail,
                            Snackbar.LENGTH_SHORT
                    ).show()
                }

                return
            }

            classDetailIds.add(classDetailId)
            rooms.add(room)
            buildings.add(building)
            teachers.add(teacher)
        }

        if (rooms.isEmpty()) {
            Log.v(LOG_TAG, "Nothing entered")
            handleCloseAction()
            return
        }

        // Now write the data (replace class detail values)

        val highestClassId = mClassHandler.getHighestItemId()
        val classId = if (mIsNew) highestClassId + 1 else mItem!!.id

        for (i in rooms.indices) {
            val classDetailId = classDetailIds[i]
            val room = rooms[i]
            val building = buildings[i]
            val teacher = teachers[i]

            val classDetail = ClassDetail(classDetailId, classId, room, building, teacher)

            mClassDetailHandler.replaceItem(classDetailId, classDetail)
        }

        val currentTimetable = (application as TimetableApplication).currentTimetable!!

        var dbStartDate = mStartDate
        var dbEndDate = mEndDate
        if (mStartDate == null) {
            dbStartDate = Class.NO_DATE
        }
        if (mEndDate == null) {
            dbEndDate = Class.NO_DATE
        }

        mItem = Class(classId,
                currentTimetable.id,
                mSubject!!.id,
                moduleName,
                dbStartDate!!,
                dbEndDate!!)

        if (mIsNew) {
            mClassHandler.addItem(mItem!!)
        } else {
            mClassHandler.replaceItem(mItem!!.id, mItem!!)
        }

        setResult(Activity.RESULT_OK)
        supportFinishAfterTransition()
    }

    override fun handleDeleteAction() {
        AlertDialog.Builder(this)
                .setTitle(R.string.delete_class)
                .setMessage(R.string.delete_confirmation_class)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    mClassHandler.deleteItemWithReferences(mItem!!.id)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

}
