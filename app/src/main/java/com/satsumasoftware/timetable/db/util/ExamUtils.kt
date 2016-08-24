package com.satsumasoftware.timetable.db.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.ExamsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.framework.Exam
import java.util.*

class ExamUtils {

    companion object {

        const val LOG_TAG = "ExamUtils"

        @JvmStatic fun getExams(activity: Activity): ArrayList<Exam> {
            val exams = ArrayList<Exam>()

            val timetable = (activity.application as TimetableApplication).currentTimetable!!

            val dbHelper = TimetableDbHelper.getInstance(activity)
            val cursor = dbHelper.readableDatabase.query(
                    ExamsSchema.TABLE_NAME,
                    null,
                    "${ExamsSchema.COL_TIMETABLE_ID}=?",
                    arrayOf(timetable.id.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                exams.add(Exam(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            return exams
        }

        @JvmStatic fun getExamWithId(context: Context, examId: Int): Exam? {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ExamsSchema.TABLE_NAME,
                    null,
                    "${ExamsSchema._ID}=?",
                    arrayOf(examId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                return null
            }
            val exam = Exam(cursor)
            cursor.close()
            return exam
        }

        @JvmStatic fun getExamsForSubject(context: Context, subjectId: Int): ArrayList<Exam> {
            val exams = ArrayList<Exam>()
            val dbHelper = TimetableDbHelper.getInstance(context)
            val cursor = dbHelper.readableDatabase.query(
                    ExamsSchema.TABLE_NAME,
                    null,
                    "${ExamsSchema.COL_SUBJECT_ID}=?",
                    arrayOf(subjectId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            exams.add(Exam(cursor))
            cursor.close()
            return exams
        }

        @JvmStatic fun addExam(context: Context, exam: Exam) {
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
            Log.i(LOG_TAG, "Added Exam with id ${exam.id}")
        }

        @JvmStatic fun deleteExam(context: Context, examId: Int) {
            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.delete(ExamsSchema.TABLE_NAME,
                    "${ExamsSchema._ID}=?",
                    arrayOf(examId.toString()))
            Log.i(LOG_TAG, "Deleted Exam with id $examId")
        }

        @JvmStatic fun replaceExam(context: Context, oldExamId: Int, newExam: Exam) {
            Log.i(LOG_TAG, "Replacing Exam...")
            deleteExam(context, oldExamId)
            addExam(context, newExam)
        }

        @JvmStatic fun getHighestExamId(context: Context): Int {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ExamsSchema.TABLE_NAME,
                    arrayOf(ExamsSchema._ID),
                    null,
                    null,
                    null,
                    null,
                    "${ExamsSchema._ID} DESC")
            if (cursor.count == 0) {
                return 0
            }
            cursor.moveToFirst()
            val highestId = cursor.getInt(cursor.getColumnIndex(ExamsSchema._ID))
            cursor.close()
            return highestId
        }

    }
}
