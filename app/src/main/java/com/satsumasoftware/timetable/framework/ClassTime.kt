package com.satsumasoftware.timetable.framework

import android.app.Activity
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.PrefUtils
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.ClassTimesSchema
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

class ClassTime(val id: Int, val timetableId: Int, val classDetailId: Int, val day: DayOfWeek,
                val weekNumber: Int, val startTime: LocalTime, val endTime: LocalTime) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassTimesSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_TIMETABLE_ID)),
            cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_CLASS_DETAIL_ID)),
            DayOfWeek.of(cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_DAY))),
            cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_WEEK_NUMBER)),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_START_TIME_MINS))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_END_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_END_TIME_MINS))))

    fun displayWeekValue(activity: Activity): String {
        val timetable = (activity.application as TimetableApplication).currentTimetable!!
        if (timetable.hasFixedScheduling()) {
            return ""
        } else {
            if (PrefUtils.displayWeeksAsLetters(activity)) {
                return when(weekNumber) {
                    1 -> "A"
                    2 -> "B"
                    3 -> "C"
                    4 -> "D"
                    else -> throw IllegalArgumentException("invalid week number '$weekNumber'")
                }
            } else {
                return weekNumber.toString()
            }
        }
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readSerializable() as DayOfWeek,
            source.readInt(),
            source.readSerializable() as LocalTime,
            source.readSerializable() as LocalTime)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeInt(classDetailId)
        dest?.writeSerializable(day)
        dest?.writeInt(weekNumber)
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
