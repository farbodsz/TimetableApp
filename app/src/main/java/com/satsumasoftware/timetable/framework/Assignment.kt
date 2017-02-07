package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.schema.AssignmentsSchema
import org.threeten.bp.LocalDate

/**
 * Represents an assignment the user may have been given.
 *
 * @property id the identifier for this assignment
 * @property timetableId the identifier for the ([Timetable]) this assignment is linked to
 * @property classId the identifier of the [Class] this assignment is associated with
 * @property title the name of the assignment
 * @property detail optional, additional notes the user may enter for the assignment
 * @property dueDate the date the assignment must be handed in
 * @property completionProgress an integer from 0-100 (like a percentage) indicating how complete
 *      the assignment is (100 indicating fully complete)
 */
class Assignment(val id: Int, val timetableId: Int, val classId: Int, val title: String,
                 val detail: String, val dueDate: LocalDate,
                 var completionProgress: Int) : BaseItem, Parcelable {

    companion object {

        /**
         * Constructs an [Assignment] using column values from the cursor provided
         *
         * @param cursor a query of the assignments table
         * @see [AssignmentsSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Assignment {
            val dueDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH)))

            return Assignment(
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_TIMETABLE_ID)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_CLASS_ID)),
                    cursor.getString(cursor.getColumnIndex(AssignmentsSchema.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(AssignmentsSchema.COL_DETAIL)),
                    dueDate,
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_COMPLETION_PROGRESS)))
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
            val assignment = Assignment.from(cursor)
            cursor.close()
            return assignment
        }

        @Suppress("unused") @JvmField val CREATOR: Parcelable.Creator<Assignment> =
                object : Parcelable.Creator<Assignment> {
                    override fun createFromParcel(source: Parcel): Assignment = Assignment(source)
                    override fun newArray(size: Int): Array<Assignment?> = arrayOfNulls(size)
                }
    }

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
  
}
