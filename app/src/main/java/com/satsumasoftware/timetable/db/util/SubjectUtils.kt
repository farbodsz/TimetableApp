package com.satsumasoftware.timetable.db.util

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.schema.SubjectsSchema
import com.satsumasoftware.timetable.framework.Subject

object SubjectUtils {

    private const val LOG_TAG = "SubjectUtils"

    @JvmStatic
    fun addSubject(context: Context, subject: Subject) {
        val values = ContentValues()
        with(values) {
            put(SubjectsSchema._ID, subject.id)
            put(SubjectsSchema.COL_TIMETABLE_ID, subject.timetableId)
            put(SubjectsSchema.COL_NAME, subject.name)
            put(SubjectsSchema.COL_ABBREVIATION, subject.abbreviation)
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

    @JvmStatic
    fun completelyDeleteSubject(context: Context, subject: Subject) {
        Log.i(LOG_TAG, "Deleting everything related to Subject with id ${subject.id}")

        deleteSubject(context, subject.id)

        for (cls in ClassUtils.getClassesForSubject(context, subject.id)) {
            ClassUtils.completelyDeleteClass(context, cls)
        }

        for (exam in ExamUtils.getExamsForSubject(context, subject.id)) {
            ExamUtils.deleteExam(context, exam.id)
        }
    }

    @JvmStatic
    fun replaceSubject(context: Context, oldSubjectId: Int, newSubject: Subject) {
        Log.i(LOG_TAG, "Replacing Subject...")
        deleteSubject(context, oldSubjectId)
        addSubject(context, newSubject)
    }

}
