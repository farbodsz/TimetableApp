package com.satsumasoftware.timetable.db.util

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.schema.ExamsSchema
import com.satsumasoftware.timetable.framework.Exam
import com.satsumasoftware.timetable.receiver.AlarmReceiver
import com.satsumasoftware.timetable.util.DateUtils
import java.util.*

object ExamUtils {

    private const val LOG_TAG = "ExamUtils"

    @JvmStatic
    fun getAllExams(context: Context): ArrayList<Exam> {
        val exams = ArrayList<Exam>()
        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                ExamsSchema.TABLE_NAME, null, null, null, null, null, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            exams.add(Exam.from(cursor))
            cursor.moveToNext()
        }
        cursor.close()
        return exams
    }

    @JvmStatic
    fun getExamsForSubject(context: Context, subjectId: Int): ArrayList<Exam> {
        val exams = ArrayList<Exam>()
        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                ExamsSchema.TABLE_NAME,
                null,
                "${ExamsSchema.COL_SUBJECT_ID}=?",
                arrayOf(subjectId.toString()),
                null, null, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            exams.add(Exam.from(cursor))
            cursor.moveToNext()
        }
        cursor.close()
        return exams
    }

    @JvmStatic
    fun addExam(context: Context, exam: Exam) {
        val values = ContentValues()
        with(values) {
            put(ExamsSchema._ID, exam.id)
            put(ExamsSchema.COL_TIMETABLE_ID, exam.timetableId)
            put(ExamsSchema.COL_SUBJECT_ID, exam.subjectId)
            put(ExamsSchema.COL_MODULE, exam.moduleName)
            put(ExamsSchema.COL_DATE_DAY_OF_MONTH, exam.date.dayOfMonth)
            put(ExamsSchema.COL_DATE_MONTH, exam.date.monthValue)
            put(ExamsSchema.COL_DATE_YEAR, exam.date.year)
            put(ExamsSchema.COL_START_TIME_HRS, exam.startTime.hour)
            put(ExamsSchema.COL_START_TIME_MINS, exam.startTime.minute)
            put(ExamsSchema.COL_DURATION, exam.duration)
            put(ExamsSchema.COL_SEAT, exam.seat)
            put(ExamsSchema.COL_ROOM, exam.room)
            put(ExamsSchema.COL_IS_RESIT, if (exam.resit) 1 else 0)
        }

        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.insert(ExamsSchema.TABLE_NAME, null, values)

        addAlarmForExam(context, exam)

        Log.i(LOG_TAG, "Added Exam with id ${exam.id}")
    }

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

    @JvmStatic
    fun replaceExam(context: Context, oldExamId: Int, newExam: Exam) {
        Log.i(LOG_TAG, "Replacing Exam...")
        deleteExam(context, oldExamId)
        addExam(context, newExam)
    }

}
