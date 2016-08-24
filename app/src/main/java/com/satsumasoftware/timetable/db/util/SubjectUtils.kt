package com.satsumasoftware.timetable.db.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.SubjectsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.framework.Subject
import java.util.*

class SubjectUtils {

    companion object {

        const val LOG_TAG = "SubjectUtils"

        @JvmStatic fun getSubjects(activity: Activity): ArrayList<Subject> {
            val subjects = ArrayList<Subject>()

            val timetable = (activity.application as TimetableApplication).currentTimetable!!

            val dbHelper = TimetableDbHelper.getInstance(activity)
            val cursor = dbHelper.readableDatabase.query(
                    SubjectsSchema.TABLE_NAME,
                    null,
                    "${SubjectsSchema.COL_TIMETABLE_ID}=?",
                    arrayOf(timetable.id.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                subjects.add(Subject(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            return subjects
        }

        @JvmStatic fun getSubjectWithId(context: Context, id: Int): Subject? {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    SubjectsSchema.TABLE_NAME,
                    null,
                    "${SubjectsSchema._ID}=?",
                    arrayOf(id.toString()),
                    null, null, null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                return null
            }
            val subject = Subject(cursor)
            cursor.close()
            return subject
        }

        @JvmStatic fun addSubject(context: Context, subject: Subject) {
            val values = ContentValues()
            with(values) {
                put(SubjectsSchema._ID, subject.id)
                put(SubjectsSchema.COL_TIMETABLE_ID, subject.timetableId)
                put(SubjectsSchema.COL_NAME, subject.name)
                put(SubjectsSchema.COL_COLOR_ID, subject.colorId)
            }

            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.insert(SubjectsSchema.TABLE_NAME, null, values)
            Log.i(LOG_TAG, "Added Subject with id ${subject.id}")
        }

        private fun deleteSubject(context: Context, subjectId: Int) {
            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.delete(SubjectsSchema.TABLE_NAME,
                    "${SubjectsSchema._ID}=?",
                    arrayOf(subjectId.toString()))
            Log.i(LOG_TAG, "Deleted Subject with id $subjectId")
        }

        @JvmStatic fun completelyDeleteSubject(context: Context, subject: Subject) {
            Log.i(LOG_TAG, "Deleting everything related to Subject with id ${subject.id}")

            deleteSubject(context, subject.id)

            for (cls in ClassUtils.getClassesForSubject(context, subject.id)) {
                ClassUtils.completelyDeleteClass(context, cls)
            }

            for (exam in ExamUtils.getExamsForSubject(context, subject.id)) {
                ExamUtils.deleteExam(context, exam.id)
            }
        }

        @JvmStatic fun replaceSubject(context: Context, oldSubjectId: Int, newSubject: Subject) {
            Log.i(LOG_TAG, "Replacing Subject...")
            deleteSubject(context, oldSubjectId)
            addSubject(context, newSubject)
        }

        @JvmStatic fun getHighestSubjectId(context: Context): Int {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    SubjectsSchema.TABLE_NAME,
                    arrayOf(SubjectsSchema._ID),
                    null,
                    null,
                    null,
                    null,
                    "${SubjectsSchema._ID} DESC")
            if (cursor.count == 0) {
                return 0
            }
            cursor.moveToFirst()
            val highestId = cursor.getInt(cursor.getColumnIndex(SubjectsSchema._ID))
            cursor.close()
            return highestId
        }

    }
}
