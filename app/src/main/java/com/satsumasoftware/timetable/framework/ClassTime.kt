package com.satsumasoftware.timetable.framework

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.ClassTimesSchema
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

class ClassTime(val id: Int, val classDetailId: Int, val day: DayOfWeek, val startTime: LocalTime,
                val endTime: LocalTime) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassTimesSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_CLASS_DETAIL_ID)),
            DayOfWeek.of(cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_DAY))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_START_TIME_MINS))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_END_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_END_TIME_MINS))))

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readSerializable() as DayOfWeek,
            source.readSerializable() as LocalTime,
            source.readSerializable() as LocalTime)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(classDetailId)
        dest?.writeSerializable(day)
        dest?.writeSerializable(startTime)
        dest?.writeSerializable(endTime)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ClassTime> = object : Parcelable.Creator<ClassTime> {
            override fun createFromParcel(source: Parcel): ClassTime = ClassTime(source)
            override fun newArray(size: Int): Array<ClassTime?> = arrayOfNulls(size)
        }
    }
}
