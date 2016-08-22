package com.satsumasoftware.timetable.db.util

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.ClassesSchema
import com.satsumasoftware.timetable.db.SubjectsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.framework.Class
import com.satsumasoftware.timetable.framework.Subject
import java.util.*

const val LOG_TAG_SUBJECT = "SubjectUtils"

fun getSubjects(context: Context): ArrayList<Subject> {
    val subjects = ArrayList<Subject>()
    val dbHelper = TimetableDbHelper.getInstance(context)
    val cursor = dbHelper.readableDatabase.query(
            SubjectsSchema.TABLE_NAME, null, null, null, null, null, null)
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        subjects.add(Subject(cursor))
        cursor.moveToNext()
    }
    cursor.close()
    return subjects
}

fun getSubjectWithId(context: Context, id: Int): Subject? {
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

fun addSubject(context: Context, subject: Subject) {
    val values = ContentValues()
    with(values) {
        put(SubjectsSchema._ID, subject.id)
        put(SubjectsSchema.COL_NAME, subject.name)
        put(SubjectsSchema.COL_COLOR_ID, subject.colorId)
    }

    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.insert(SubjectsSchema.TABLE_NAME, null, values)
    Log.i(LOG_TAG_SUBJECT, "Added Subject with id ${subject.id}")
}

private fun deleteSubject(context: Context, subjectId: Int) {
    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.delete(SubjectsSchema.TABLE_NAME,
            "${SubjectsSchema._ID}=?",
            arrayOf(subjectId.toString()))
    Log.i(LOG_TAG_SUBJECT, "Deleted Subject with id $subjectId")
}

fun completelyDeleteSubject(context: Context, subject: Subject) {
    Log.i(LOG_TAG_SUBJECT, "Deleting everything related to Subject with id ${subject.id}")

    deleteSubject(context, subject.id)

    for (cls in getClassesForSubject(context, subject.id)) {
        completelyDeleteClass(context, cls)
    }
}

fun replaceSubject(context: Context, oldSubjectId: Int, newSubject: Subject) {
    Log.i(LOG_TAG_SUBJECT, "Replacing Subject...")
    deleteSubject(context, oldSubjectId)
    addSubject(context, newSubject)
}

fun getHighestSubjectId(context: Context): Int {
    val db = TimetableDbHelper.getInstance(context).readableDatabase
    val cursor = db.query(
            SubjectsSchema.TABLE_NAME, null, null, null, null, null, null)
    val count = cursor.count
    cursor.close()
    return count
}
