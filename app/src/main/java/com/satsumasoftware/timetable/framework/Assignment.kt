package com.satsumasoftware.timetable.framework

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.AssignmentsSchema
import org.threeten.bp.LocalDate

class Assignment(val id: Int, val classId: Int, val title: String, val detail: String,
                 val dueDate: LocalDate, var completionProgress: Int) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(AssignmentsSchema._ID)),
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
            source.readString(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readInt())

    fun hasDetail() = detail.trim().length != 0

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
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
    }
}
