package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.AssignmentsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import org.threeten.bp.LocalDate

/**
 * Represents an assignment the user may have been given.
 *
 * Assignments have an id, title and due date. They also store a detail string which the user can
 * use to add notes about his/her assignment. Users can mark how much of an assignment they have
 * completed as a percentage, which changes the `completionProgress` property. So to mark it as
 * "Done", the `completionProgress` will be shown as 100.
 *
 * Since assignments are given for different classes, there is a `classId` property to provide a
 * link between it and the class.
 *
 * Similarly, a `timetableId` is used to identify which academic year (`Timetable`) it is linked to.
 *
 * It is important to remember that this class is only used to represent the data from the SQLite
 * database - it does not store any permanent data itself.
 */
class Assignment(val id: Int, val timetableId: Int, val classId: Int, val title: String,
                 val detail: String, val dueDate: LocalDate, var completionProgress: Int) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(AssignmentsSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_TIMETABLE_ID)),
            cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_CLASS_ID)),
            cursor.getString(cursor.getColumnIndex(AssignmentsSchema.COL_TITLE)),
            cursor.getString(cursor.getColumnIndex(AssignmentsSchema.COL_DETAIL)),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH))),
            cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_COMPLETION_PROGRESS)))

    constructor(source: Parcel): this(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readInt())

    fun hasDetail() = detail.trim().isNotEmpty()

    fun isComplete() = completionProgress == 100

    fun isOverdue() = !isComplete() && dueDate.isBefore(LocalDate.now())

    fun isPastAndDone() = dueDate.isBefore(LocalDate.now()) && completionProgress == 100

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeInt(classId)
        dest?.writeString(title)
        dest?.writeString(detail)
        dest?.writeSerializable(dueDate)
        dest?.writeInt(completionProgress)
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<Assignment> = object : Parcelable.Creator<Assignment> {
            override fun createFromParcel(source: Parcel): Assignment = Assignment(source)
            override fun newArray(size: Int): Array<Assignment?> = arrayOfNulls(size)
        }

        @JvmStatic
        fun create(context: Context, assignmentId: Int): Assignment? {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    AssignmentsSchema.TABLE_NAME,
                    null,
                    "${AssignmentsSchema._ID}=?",
                    arrayOf(assignmentId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                return null
            }
            val assignment = Assignment(cursor)
            cursor.close()
            return assignment
        }

    }
}
