package com.satsumasoftware.timetable

import android.app.Activity
import android.app.Application
import android.util.Log

import com.jakewharton.threetenabp.AndroidThreeTen
import com.satsumasoftware.timetable.db.util.AssignmentUtils
import com.satsumasoftware.timetable.db.util.ClassUtils
import com.satsumasoftware.timetable.db.util.ExamUtils
import com.satsumasoftware.timetable.framework.Timetable

class TimetableApplication : Application() {

    private val LOG_TAG = "TimetableApplication"

    var currentTimetable: Timetable? = null
        set(value) {
            field = value
            value?.let {
                PrefUtils.setCurrentTimetable(this, field!!)
                Log.i(LOG_TAG, "Switched current timetable to that with id ${field!!.id}")
            }
        }

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        currentTimetable = PrefUtils.getCurrentTimetable(this)
    }

    fun refreshAlarms(activity: Activity) {
        // Cancel alarms from all timetables and add alarms for the current one
        val alarmReceiver = AlarmReceiver()

        Log.i(LOG_TAG, "Cancelling ALL alarms")
        for (classTime in ClassUtils.getAllClassTimes(activity)) {
            alarmReceiver.cancelAlarm(activity, AlarmReceiver.Type.CLASS, classTime.id)
        }
        for (assignment in AssignmentUtils.getAllAssignments(activity)) {
            alarmReceiver.cancelAlarm(activity, AlarmReceiver.Type.ASSIGNMENT, assignment.id)
        }
        for (exam in ExamUtils.getAllExams(activity)) {
            alarmReceiver.cancelAlarm(activity, AlarmReceiver.Type.EXAM, exam.id)
        }

        Log.i(LOG_TAG, "Adding alarms for the current timetable (id: ${currentTimetable!!.id})")
        for (classTime in ClassUtils.getAllClassTimes(activity, currentTimetable!!)) {
            ClassUtils.addAlarmsForClassTime(activity, classTime)
        }
        for (assignment in AssignmentUtils.getAssignments(activity)) {
            AssignmentUtils.addAlarmForAssignment(activity, assignment)
        }
        for (exam in ExamUtils.getExams(activity)) {
            ExamUtils.addAlarmForExam(activity, exam)
        }
    }

}
