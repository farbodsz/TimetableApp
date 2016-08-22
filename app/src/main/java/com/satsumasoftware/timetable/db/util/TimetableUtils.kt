package com.satsumasoftware.timetable.db.util

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.TimetablesSchema
import com.satsumasoftware.timetable.framework.Timetable
import java.util.*

const val LOG_TAG_TIMETABLE = "TimetableUtils"

fun getTimetables(context: Context): ArrayList<Timetable> {
    val timetables = ArrayList<Timetable>()
    val dbHelper = TimetableDbHelper.getInstance(context)
    val cursor = dbHelper.readableDatabase.query(
            TimetablesSchema.TABLE_NAME, null, null, null, null, null, null)
    cursor.moveToFirst()
    while (!cursor.isAfterLast) {
        timetables.add(Timetable(cursor))
        cursor.moveToNext()
    }
    cursor.close()
    return timetables
}

fun getTimetableWithId(context: Context, timetableId: Int): Timetable? {
    val db = TimetableDbHelper.getInstance(context).readableDatabase
    val cursor = db.query(
            TimetablesSchema.TABLE_NAME,
            null,
            "${TimetablesSchema._ID}=?",
            arrayOf(timetableId.toString()),
            null, null, null)
    cursor.moveToFirst()
    if (cursor.count == 0) {
        cursor.close()
        return null
    }
    val timetable = Timetable(cursor)
    cursor.close()
    return timetable
}

fun addTimetable(context: Context, timetable: Timetable) {
    val values = ContentValues()
    with(values) {
        put(TimetablesSchema._ID, timetable.id)
        put(TimetablesSchema.COL_NAME, timetable.name)
        put(TimetablesSchema.COL_START_DATE_DAY_OF_MONTH, timetable.startDate.dayOfMonth)
        put(TimetablesSchema.COL_START_DATE_MONTH, timetable.startDate.monthValue)
        put(TimetablesSchema.COL_START_DATE_YEAR, timetable.startDate.year)
        put(TimetablesSchema.COL_END_DATE_DAY_OF_MONTH, timetable.endDate.dayOfMonth)
        put(TimetablesSchema.COL_END_DATE_MONTH, timetable.endDate.monthValue)
        put(TimetablesSchema.COL_END_DATE_YEAR, timetable.endDate.year)
    }

    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.insert(TimetablesSchema.TABLE_NAME, null, values)
    Log.i(LOG_TAG_TIMETABLE, "Added Timetable with id ${timetable.id}")
}

fun deleteTimetable(context: Context, timetableId: Int) {
    val db = TimetableDbHelper.getInstance(context).writableDatabase
    db.delete(TimetablesSchema.TABLE_NAME,
            "${TimetablesSchema._ID}=?",
            arrayOf(timetableId.toString()))
    Log.i(LOG_TAG_TIMETABLE, "Deleted Timetable with id $timetableId")
}

fun replaceTimetable(context: Context, oldTimetableId: Int, newTimetable: Timetable) {
    Log.i(LOG_TAG_TIMETABLE, "Replacing Timetable...")
    deleteTimetable(context, oldTimetableId)
    addTimetable(context, newTimetable)
}

fun getHighestTimetableId(context: Context): Int {
    val db = TimetableDbHelper.getInstance(context).readableDatabase
    val cursor = db.query(
            TimetablesSchema.TABLE_NAME, null, null, null, null, null, null)
    val count = cursor.count
    cursor.close()
    return count
}
