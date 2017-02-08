package com.satsumasoftware.timetable.db.util

import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.schema.ExamsSchema
import com.satsumasoftware.timetable.framework.Exam
import com.satsumasoftware.timetable.receiver.AlarmReceiver
import com.satsumasoftware.timetable.util.DateUtils

object ExamUtils {

    private const val LOG_TAG = "ExamUtils"

    @JvmStatic
    fun addAlarmForExam(context: Context, exam: Exam) {
        val remindDate = exam.makeDateTimeObject().minusMinutes(30)
        AlarmReceiver().setAlarm(context,
                AlarmReceiver.Type.EXAM,
                DateUtils.asCalendar(remindDate),
                exam.id)
    }

    @JvmStatic
    fun deleteExam(context: Context, examId: Int) {
        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.delete(ExamsSchema.TABLE_NAME,
                "${ExamsSchema._ID}=?",
                arrayOf(examId.toString()))

        AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.EXAM, examId)

        Log.i(LOG_TAG, "Deleted Exam with id $examId")
    }

}
