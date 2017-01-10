package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.db.ClassDetailsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper

/**
 * An object to represent one set of details of a class.
 *
 * These details include the location of the class and the teacher's name.
 *
 * The reason we represent the data in this way is because a class can take place in more than one
 * location, or be taught by more than one different teacher. Therefore, a `Class` can be linked to
 * multiple `ClassDetail`s; this is why our `ClassDetail` contains a `classId` property.
 *
 * @property id the identifier for this class detail
 * @property classId the identifier of the associated [Class]
 * @property room an optional name of the room where the class takes place
 * @property building an optional name of the building where the class takes place
 * @property teacher an optional name of the teacher for the class
 */
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

    fun hasRoom() = room.trim().isNotEmpty()

    fun hasBuilding() = building.trim().isNotEmpty()

    fun hasTeacher() = teacher.trim().isNotEmpty()

}
