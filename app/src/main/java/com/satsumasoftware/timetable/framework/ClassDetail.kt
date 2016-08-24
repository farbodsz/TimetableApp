package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.db.ClassDetailsSchema
import com.satsumasoftware.timetable.db.util.ClassUtils
import java.util.*

class ClassDetail(val id: Int, val classId: Int, val room: String, val building: String,
                  val teacher: String, val classTimeIds: ArrayList<Int>) {

    constructor(context: Context, cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema.COL_CLASS_ID)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_ROOM)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_BUILDING)),
            cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_TEACHER)),
            ClassUtils.getClassTimeIds(context, cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema._ID))))

    fun hasRoom() = room.trim().length != 0

    fun hasBuilding() = building.trim().length != 0

    fun hasTeacher() = teacher.trim().length != 0

}
