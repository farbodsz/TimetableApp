package com.satsumasoftware.timetable

import android.app.Application
import android.content.Context
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

    fun refreshAlarms(context: Context) {
        // Cancel alarms from all timetables and add alarms for the current one
        val alarmReceiver = AlarmReceiver()

        Log.i(LOG_TAG, "Cancelling ALL alarms")
        for (classTime in ClassUtils.getAllClassTimes(context)) {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.CLASS, classTime.id)
        }
        for (assignment in AssignmentUtils.getAllAssignments(context)) {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.ASSIGNMENT, assignment.id)
        }
        for (exam in ExamUtils.getAllExams(context)) {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.EXAM, exam.id)
        }

        Log.i(LOG_TAG, "Adding alarms for the current timetable (id: ${currentTimetable!!.id})")
        for (classTime in ClassUtils.getAllClassTimes(context, currentTimetable!!)) {
            ClassUtils.addAlarmsForClassTime(context, this, classTime)
        }
        for (assignment in AssignmentUtils.getAssignments(context, this)) {
            AssignmentUtils.addAlarmForAssignment(context, assignment)
        }
        for (exam in ExamUtils.getExams(context, this)) {
            ExamUtils.addAlarmForExam(context, exam)
        }
    }

}
