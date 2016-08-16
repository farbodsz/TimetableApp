package com.satsumasoftware.timetable.db.util

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.AssignmentsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.framework.Assignment
import java.util.*

const val LOG_TAG_ASSIGNMENT = "AssignmentUtils"

fun getAssignments(context: Context): ArrayList<Assignment> {
    val assignments = ArrayList<Assignment>()
    val dbHelper = TimetableDbHelper.getInstance(context)
    val cursor = dbHelper.readableDatabase.query(
            AssignmentsSchema.TABLE_NAME, null, null, null, null, null, null)
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        assignments.add(Assignment(cursor))
        cursor.moveToNext()
    }
    cursor.close()
    return assignments
}

fun getAssignmentsForClass(context: Context, classId: Int): ArrayList<Assignment> {
    val assignments = ArrayList<Assignment>()
    val dbHelper = TimetableDbHelper.getInstance(context)
    val cursor = dbHelper.readableDatabase.query(
            AssignmentsSchema.TABLE_NAME,
            null,
            "${AssignmentsSchema.COL_CLASS_ID}=?",
            arrayOf(classId.toString()),
            null, null, null)
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        assignments.add(Assignment(cursor))
        cursor.moveToNext()
    }
    cursor.close()
    return assignments
}

fun addAssignment(context: Context, assignment: Assignment) {
    val values = ContentValues()
    with(values) {
        put(AssignmentsSchema._ID, assignment.id)
        put(AssignmentsSchema.COL_CLASS_ID, assignment.classId)
        put(AssignmentsSchema.COL_TITLE, assignment.title)
        put(AssignmentsSchema.COL_DETAIL, assignment.detail)
        put(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH, assignment.dueDate.dayOfMonth)
        put(AssignmentsSchema.COL_DUE_DATE_MONTH, assignment.dueDate.monthValue)
        put(AssignmentsSchema.COL_DUE_DATE_YEAR, assignment.dueDate.year)
        put(AssignmentsSchema.COL_COMPLETION_PROGRESS, assignment.completionProgress)
    }

    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.insert(AssignmentsSchema.TABLE_NAME, null, values)
    Log.i(LOG_TAG_ASSIGNMENT, "Added Assignment with id ${assignment.id}")
}

fun deleteAssignment(context: Context, assignmentId: Int) {
    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.delete(AssignmentsSchema.TABLE_NAME,
            "${AssignmentsSchema._ID}=?",
            arrayOf(assignmentId.toString()))
    Log.i(LOG_TAG_ASSIGNMENT, "Deleted Assignment with id $assignmentId")
}

fun replaceAssignment(context: Context, oldAssignmentId: Int, newAssignment: Assignment) {
    Log.i(LOG_TAG_ASSIGNMENT, "Replacing Assignment...")
    deleteAssignment(context, oldAssignmentId)
    addAssignment(context, newAssignment)
}

fun getHighestAssignmentId(context: Context): Int {
    val db = TimetableDbHelper.getInstance(context).readableDatabase
    val cursor = db.query(
            AssignmentsSchema.TABLE_NAME, null, null, null, null, null, null)
    val count = cursor.count
    cursor.close()
    return count
}
