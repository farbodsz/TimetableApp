package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.db.ClassDetailsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper

class ClassDetail(val id: Int, val classId: Int, val room: String, val building: String,
                  val teacher: String) {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema.COL_CLASS_ID)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_ROOM)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_BUILDING)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_TEACHER)))

    companion object {

        @JvmStatic
        fun create(context: Context, classDetailId: Int): ClassDetail {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassDetailsSchema.TABLE_NAME,
                    null,
                    "${ClassDetailsSchema._ID}=?",
                    arrayOf(classDetailId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            val classDetail = ClassDetail(cursor)
            cursor.close()
            return classDetail
        }
    }

    fun hasRoom() = room.trim().length != 0

    fun hasBuilding() = building.trim().length != 0

    fun hasTeacher() = teacher.trim().length != 0

}
