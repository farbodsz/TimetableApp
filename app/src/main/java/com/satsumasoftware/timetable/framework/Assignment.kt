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
 * @property id An identifier for the assignment
 * @property timetableId Used to identifier which academic year ([Timetable]) this assignment is
 *      linked to
 * @property classId The identifier of the [Class] this assignment is for
 * @property title The name of the assignment
 * @property detail Optional, additional notes the user may enter for the assignment
 * @property dueDate The date the assignment must be handed in
 * @property completionProgress An integer from 0-100 (like a percentage) indicating how complete
 *      the assignment is (100 indicating fully complete)
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
