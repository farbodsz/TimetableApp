package com.satsumasoftware.timetable.framework

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.SubjectsSchema

class Subject(val id: Int, val timetableId: Int, var name: String, var colorId: Int) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(SubjectsSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(SubjectsSchema.COL_TIMETABLE_ID)),
            cursor.getString(cursor.getColumnIndex(SubjectsSchema.COL_NAME)),
            cursor.getInt(cursor.getColumnIndex(SubjectsSchema.COL_COLOR_ID)))

    constructor(source: Parcel) : this(
            source.readInt(), source.readInt(), source.readString(), source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(name)
        dest?.writeInt(colorId)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Subject> = object : Parcelable.Creator<Subject> {
            override fun createFromParcel(source: Parcel): Subject = Subject(source)
            override fun newArray(size: Int): Array<Subject?> = arrayOfNulls(size)
        }
    }
}
