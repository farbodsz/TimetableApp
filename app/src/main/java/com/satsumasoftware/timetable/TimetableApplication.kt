package com.satsumasoftware.timetable

import android.app.Application
import android.content.Context
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import com.satsumasoftware.timetable.db.handler.AssignmentHandler
import com.satsumasoftware.timetable.db.handler.ClassTimeHandler
import com.satsumasoftware.timetable.db.handler.ExamHandler
import com.satsumasoftware.timetable.framework.Timetable
import com.satsumasoftware.timetable.receiver.AlarmReceiver
import com.satsumasoftware.timetable.util.PrefUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class TimetableApplication : Application() {

    private val LOG_TAG = "TimetableApplication"

    var currentTimetable: Timetable? = null
        private set(value) {
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

    fun setCurrentTimetable(context: Context, timetable: Timetable?) {
        currentTimetable = timetable
        refreshAlarms(context)
    }

    fun refreshAlarms(context: Context) {
        Log.i(LOG_TAG, "Refreshing alarms...")

        // Cancel alarms from all timetables and add alarms for the current one
        val alarmReceiver = AlarmReceiver()

        val classTimeUtils = ClassTimeHandler(context)
        val examUtils = ExamHandler(context)

        Log.i(LOG_TAG, "Cancelling ALL alarms")
        classTimeUtils.getAllItems().forEach {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.CLASS, it.id)
        }
        examUtils.getAllItems().forEach {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.EXAM, it.id)
        }

        Log.i(LOG_TAG, "Adding alarms for the current timetable (id: ${currentTimetable!!.id})")
        classTimeUtils.getItems(this).forEach {
            ClassTimeHandler.addAlarmsForClassTime(context, this, it)
        }
        examUtils.getItems(this).forEach { exam ->
            if (exam.date.isAfter(LocalDate.now()) ||
                    (exam.date.isEqual(LocalDate.now()) && exam.startTime.isAfter(LocalTime.now()))) {
                ExamHandler.addAlarmForExam(context, exam)
            }
        }

        AssignmentHandler.setAssignmentAlarmTime(
                context, PrefUtils.getAssignmentNotificationTime(context))
    }

}
