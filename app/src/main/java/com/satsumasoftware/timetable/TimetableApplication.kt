package com.satsumasoftware.timetable

import android.app.Application
import android.content.Context
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import com.satsumasoftware.timetable.db.DataHandlers
import com.satsumasoftware.timetable.db.DataUtils
import com.satsumasoftware.timetable.db.util.AssignmentUtils
import com.satsumasoftware.timetable.db.util.ClassUtils
import com.satsumasoftware.timetable.db.util.ExamUtils
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

        Log.i(LOG_TAG, "Cancelling ALL alarms")
        ClassUtils.getAllClassTimes(context).forEach {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.CLASS, it.id)
        }
        ExamUtils.getAllExams(context).forEach {
            alarmReceiver.cancelAlarm(context, AlarmReceiver.Type.EXAM, it.id)
        }

        Log.i(LOG_TAG, "Adding alarms for the current timetable (id: ${currentTimetable!!.id})")
        ClassUtils.getAllClassTimes(context, currentTimetable!!).forEach {
            ClassUtils.addAlarmsForClassTime(context, this, it)
        }
        DataUtils.getItems(DataHandlers.EXAMS, context, this).forEach { exam ->
            if (exam.date.isAfter(LocalDate.now()) ||
                    (exam.date.isEqual(LocalDate.now()) && exam.startTime.isAfter(LocalTime.now()))) {
                ExamUtils.addAlarmForExam(context, exam)
            }
        }

        AssignmentUtils.setAssignmentAlarmTime(
                context, PrefUtils.getAssignmentNotificationTime(context))
    }

}
