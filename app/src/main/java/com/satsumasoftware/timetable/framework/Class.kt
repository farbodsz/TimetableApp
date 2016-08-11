package com.satsumasoftware.timetable.framework

import java.util.*
import android.os.Parcel
import android.os.Parcelable

import android.database.Cursor
import com.satsumasoftware.timetable.db.ClassesSchema
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

class Class(val id: Int, val subjectId: Int, val day: DayOfWeek, val startTime: LocalTime,
            val endTime: LocalTime, val room: String, val teacher: String) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_SUBJECT_ID)),
            DayOfWeek.of(cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_DAY))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_TIME_MINS))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_TIME_MINS))),
            cursor.getString(cursor.getColumnIndex(ClassesSchema.COL_ROOM)),
            cursor.getString(cursor.getColumnIndex(ClassesSchema.COL_TEACHER)))

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readSerializable() as DayOfWeek,
            source.readSerializable() as LocalTime,
            source.readSerializable() as LocalTime,
            source.readString(),
            source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(subjectId)
        dest?.writeSerializable(day)
        dest?.writeSerializable(startTime)
        dest?.writeSerializable(endTime)
        dest?.writeString(room)
        dest?.writeString(teacher)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Class> = object : Parcelable.Creator<Class> {
            override fun createFromParcel(source: Parcel): Class = Class(source)
            override fun newArray(size: Int): Array<Class?> = arrayOfNulls(size)
        }
    }
}
