package com.satsumasoftware.timetable.db.util

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.ClassDetailsSchema
import com.satsumasoftware.timetable.db.ClassTimesSchema
import com.satsumasoftware.timetable.db.ClassesSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.framework.Class
import com.satsumasoftware.timetable.framework.ClassDetail
import com.satsumasoftware.timetable.framework.ClassTime
import java.util.*

const val LOG_TAG_CLASS = "ClassUtils"

fun getClasses(context: Context): ArrayList<Class> {
    val classes = ArrayList<Class>()
    val dbHelper = TimetableDbHelper.getInstance(context)
    val cursor = dbHelper.readableDatabase.query(
            ClassesSchema.TABLE_NAME, null, null, null, null, null, null)
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        classes.add(Class(context, cursor))
        cursor.moveToNext()
    }
    cursor.close()

    return classes
}

fun getClassWithId(context: Context, classId: Int): Class? {
    val dbHelper = TimetableDbHelper.getInstance(context)
    val cursor = dbHelper.readableDatabase.query(
            ClassesSchema.TABLE_NAME,
            null,
            "${ClassesSchema._ID}=?",
            arrayOf(classId.toString()),
            null, null, null)
    cursor.moveToFirst()
    if (cursor.count == 0) {
        cursor.close()
        return null
    }
    val cls = Class(context, cursor)
    cursor.close()
    return cls
}

fun getClassDetailIds(context: Context, classId: Int): ArrayList<Int> {
    val classDetailIds = ArrayList<Int>()
    val dbHelper = TimetableDbHelper.getInstance(context)
    val cursor = dbHelper.readableDatabase.query(
            ClassDetailsSchema.TABLE_NAME,
            null,
            "${ClassDetailsSchema.COL_CLASS_ID}=?",
            arrayOf(classId.toString()),
            null, null, null)
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        classDetailIds.add(cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema._ID)))
        cursor.moveToNext()
    }
    cursor.close()
    return classDetailIds
}

fun getClassDetailsFromIds(context: Context, classDetailIds: ArrayList<Int>): ArrayList<ClassDetail> {
    val classDetails = ArrayList<ClassDetail>()
    val db = TimetableDbHelper.getInstance(context).readableDatabase

    for (classDetailId in classDetailIds) {
        val cursor = db.query(
                ClassDetailsSchema.TABLE_NAME,
                null,
                "${ClassDetailsSchema._ID}=?",
                arrayOf(classDetailId.toString()),
                null, null, null)
        cursor.moveToFirst()
        classDetails.add(ClassDetail(context, cursor))
        cursor.close()
    }
    return classDetails
}

fun getClassTimeIds(context: Context, classDetailId: Int): ArrayList<Int> {
    val classTimeIds = ArrayList<Int>()
    val dbHelper = TimetableDbHelper.getInstance(context)
    val cursor = dbHelper.readableDatabase.query(
            ClassTimesSchema.TABLE_NAME,
            null,
            "${ClassTimesSchema.COL_CLASS_DETAIL_ID}=?",
            arrayOf(classDetailId.toString()),
            null, null, null)
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        classTimeIds.add(cursor.getInt(cursor.getColumnIndex(ClassTimesSchema._ID)))
        cursor.moveToNext()
    }
    cursor.close()
    return classTimeIds
}

fun getClassTimesFromIds(context: Context, classTimeIds: ArrayList<Int>): ArrayList<ClassTime> {
    val classTimes = ArrayList<ClassTime>()
    val db = TimetableDbHelper.getInstance(context).readableDatabase

    for (classTimeId in classTimeIds) {
        val cursor = db.query(
                ClassTimesSchema.TABLE_NAME,
                null,
                "${ClassTimesSchema._ID}=?",
                arrayOf(classTimeId.toString()),
                null, null, null)
        cursor.moveToFirst()
        classTimes.add(ClassTime(cursor))
        cursor.close()
    }
    return classTimes
}

fun getHighestClassId(context: Context): Int {
    val db = TimetableDbHelper.getInstance(context).readableDatabase
    val cursor = db.query(
            ClassesSchema.TABLE_NAME, null, null, null, null, null, null)
    val count = cursor.count
    cursor.close()
    return count
}

fun addClass(context: Context, cls: Class) {
    val values = ContentValues()
    with(values) {
        put(ClassesSchema._ID, cls.id)
        put(ClassesSchema.COL_SUBJECT_ID, cls.subjectId)
    }

    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.insert(ClassesSchema.TABLE_NAME, null, values)
    Log.i(LOG_TAG_CLASS, "Added Class with id ${cls.id}")
}

private fun deleteClass(context: Context, classId: Int) {
    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.delete(ClassesSchema.TABLE_NAME,
            "${ClassesSchema._ID}=?",
            arrayOf(classId.toString()))
    Log.i(LOG_TAG_CLASS, "Deleted Class with id $classId")
}

fun replaceClass(context: Context, oldClassId: Int, newClass: Class) {
    Log.i(LOG_TAG_CLASS, "Replacing Class...")
    deleteClass(context, oldClassId)
    addClass(context, newClass)
}

fun getHighestClassDetailId(context: Context): Int {
    val db = TimetableDbHelper.getInstance(context).readableDatabase
    val cursor = db.query(
            ClassDetailsSchema.TABLE_NAME, null, null, null, null, null, null)
    val count = cursor.count
    cursor.close()
    return count
}

fun addClassDetail(context: Context, classDetail: ClassDetail) {
    val values = ContentValues()
    with(values) {
        put(ClassDetailsSchema._ID, classDetail.id)
        put(ClassDetailsSchema.COL_CLASS_ID, classDetail.classId)
        put(ClassDetailsSchema.COL_ROOM, classDetail.room)
        put(ClassDetailsSchema.COL_TEACHER, classDetail.teacher)
    }

    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.insert(ClassDetailsSchema.TABLE_NAME, null, values)
    Log.i(LOG_TAG_CLASS, "Added ClassDetail with id ${classDetail.id}")
}

private fun deleteClassDetail(context: Context, classDetailId: Int) {
    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.delete(ClassDetailsSchema.TABLE_NAME,
            "${ClassDetailsSchema._ID}=?",
            arrayOf(classDetailId.toString()))
    Log.i(LOG_TAG_CLASS, "Deleted ClassDetail with id $classDetailId")
}

fun replaceClassDetail(context: Context, oldClassDetailId: Int, newClassDetail: ClassDetail) {
    Log.i(LOG_TAG_CLASS, "Replacing ClassDetail...")
    deleteClassDetail(context, oldClassDetailId)
    addClassDetail(context, newClassDetail)
}

fun getHighestClassTimeId(context: Context): Int {
    val db = TimetableDbHelper.getInstance(context).readableDatabase
    val cursor = db.query(
            ClassTimesSchema.TABLE_NAME, null, null, null, null, null, null)
    val count = cursor.count
    cursor.close()
    return count
}

fun addClassTime(context: Context, classTime: ClassTime) {
    val values = ContentValues()
    with(values) {
        put(ClassTimesSchema._ID, classTime.id)
        put(ClassTimesSchema.COL_CLASS_DETAIL_ID, classTime.classDetailId)
        put(ClassTimesSchema.COL_DAY, classTime.day.value)
        put(ClassTimesSchema.COL_START_TIME_HRS, classTime.startTime.hour)
        put(ClassTimesSchema.COL_START_TIME_MINS, classTime.startTime.minute)
        put(ClassTimesSchema.COL_END_TIME_HRS, classTime.endTime.hour)
        put(ClassTimesSchema.COL_END_TIME_MINS, classTime.endTime.minute)
    }

    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.insert(ClassTimesSchema.TABLE_NAME, null, values)
    Log.i(LOG_TAG_CLASS, "Added ClassTime with id ${classTime.id}")
}

private fun deleteClassTime(context: Context, classTimeId: Int) {
    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.delete(ClassTimesSchema.TABLE_NAME,
            "${ClassTimesSchema._ID}=?",
            arrayOf(classTimeId.toString()))
    Log.i(LOG_TAG_CLASS, "Deleted ClassTime with id $classTimeId")
}

fun replaceClassTime(context: Context, oldClassTimeId: Int, newClassTime: ClassTime) {
    Log.i(LOG_TAG_CLASS, "Replacing ClassTime...")
    deleteClassTime(context, oldClassTimeId)
    addClassTime(context, newClassTime)
}

fun completelyDeleteClass(context: Context, cls: Class) {
    Log.i(LOG_TAG_CLASS, "Deleting everything related to Class of id ${cls.id}")

    deleteClass(context, cls.id)

    for (assignment in getAssignmentsForClass(context, cls.id)) {
        deleteAssignment(context, assignment.id)
    }

    for (classDetailId in cls.classDetailIds) {
        completelyDeleteClassDetail(context, classDetailId)
    }
}

fun completelyDeleteClassDetail(context: Context, classDetailId: Int) {
    Log.i(LOG_TAG_CLASS, "Deleting everything related to ClassDetail of id $classDetailId")

    deleteClassDetail(context, classDetailId)

    for (classTimeId in getClassTimeIds(context, classDetailId)) {
        completelyDeleteClassTime(context, classTimeId)
    }
}

fun completelyDeleteClassTime(context: Context, classTimeId: Int) {
    Log.i(LOG_TAG_CLASS, "Deleting everything related to ClassTime of id $classTimeId")

    deleteClassTime(context, classTimeId)
}
