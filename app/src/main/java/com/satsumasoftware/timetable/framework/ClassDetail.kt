package com.satsumasoftware.timetable.framework

import android.database.Cursor
import com.satsumasoftware.timetable.db.ClassDetailsSchema

class ClassDetail(val id: Int, val classId: Int, val room: String, val building: String,
                  val teacher: String) {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema.COL_CLASS_ID)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_ROOM)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_BUILDING)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_TEACHER)))

    fun hasRoom() = room.trim().length != 0

    fun hasBuilding() = building.trim().length != 0

    fun hasTeacher() = teacher.trim().length != 0

}
