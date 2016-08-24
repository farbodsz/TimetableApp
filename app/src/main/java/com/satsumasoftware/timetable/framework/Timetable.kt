package com.satsumasoftware.timetable.framework

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.TimetablesSchema
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class Timetable(val id: Int, val name: String, val startDate: LocalDate,
                val endDate: LocalDate) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(TimetablesSchema._ID)),
            cursor.getString(cursor.getColumnIndex(TimetablesSchema.COL_NAME)),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_YEAR))),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_YEAR))))

    fun hasName() = name.trim().length != 0

    fun makeDefaultName(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM uuuu")
        return "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readSerializable() as LocalDate)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(name)
        dest?.writeSerializable(startDate)
        dest?.writeSerializable(endDate)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Timetable> = object : Parcelable.Creator<Timetable> {
            override fun createFromParcel(source: Parcel): Timetable = Timetable(source)
            override fun newArray(size: Int): Array<Timetable?> = arrayOfNulls(size)
        }
    }
}
