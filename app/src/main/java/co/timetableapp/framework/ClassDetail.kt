package co.timetableapp.framework

import android.content.Context
import android.database.Cursor
import co.timetableapp.db.TimetableDbHelper
import co.timetableapp.db.schema.ClassDetailsSchema

/**
 * An object to represent one set of details of a class.
 *
 * These details include the location of the class and the teacher's name.
 *
 * The reason we represent the data in this way is because a class can take place in more than one
 * location, or be taught by more than one different teacher. Therefore, a `Class` can be linked to
 * multiple `ClassDetail`s; this is why our `ClassDetail` contains a `classId` property.
 *
 * @property classId the identifier of the associated [Class]
 * @property room an optional name of the room where the class takes place
 * @property building an optional name of the building where the class takes place
 * @property teacher an optional name of the teacher for the class
 */
class ClassDetail(override val id: Int, val classId: Int, val room: String, val building: String,
                  val teacher: String) : BaseItem {

    companion object {

        /**
         * Constructs a [ClassDetail] using column values from the cursor provided
         *
         * @param cursor a query of the class details table
         * @see [ClassDetailsSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): ClassDetail {
            return ClassDetail(
                    cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(ClassDetailsSchema.COL_CLASS_ID)),
                    cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_ROOM)),
                    cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_BUILDING)),
                    cursor.getString(cursor.getColumnIndex(ClassDetailsSchema.COL_TEACHER)))
        }

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
            val classDetail = from(cursor)
            cursor.close()
            return classDetail
        }
    }

    fun hasRoom() = room.trim().isNotEmpty()

    fun hasBuilding() = building.trim().isNotEmpty()

    fun hasTeacher() = teacher.trim().isNotEmpty()

}
