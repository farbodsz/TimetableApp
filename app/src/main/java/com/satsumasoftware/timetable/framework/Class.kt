package com.satsumasoftware.timetable.framework

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.ClassesSchema

class Class(val id: Int, val timetableId: Int, val subjectId: Int) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassesSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_TIMETABLE_ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_SUBJECT_ID)))

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeInt(subjectId)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Class> = object : Parcelable.Creator<Class> {
            override fun createFromParcel(source: Parcel): Class = Class(source)
            override fun newArray(size: Int): Array<Class?> = arrayOfNulls(size)
        }
    }
}
