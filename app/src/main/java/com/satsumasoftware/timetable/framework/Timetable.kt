package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.TimetablesSchema
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

class Timetable(val id: Int, val name: String, val startDate: LocalDate, val endDate: LocalDate,
                val weekRotations: Int) : Parcelable {

    val displayedName: String
        get() {
            return if (hasName()) {
                name
            } else {
                val formatter = DateTimeFormatter.ofPattern("MMM uuuu")
                "${startDate.format(formatter)} - ${endDate.format(formatter)}"
            }
        }

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(TimetablesSchema._ID)),
            cursor.getString(cursor.getColumnIndex(TimetablesSchema.COL_NAME)),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_DAY_OF_MONTH))),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_DAY_OF_MONTH))),
            cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_WEEK_ROTATIONS)))

    fun hasName() = name.trim().length != 0

    fun hasFixedScheduling() = weekRotations == 1

    fun isValidToday() = !LocalDate.now().isBefore(startDate) && !LocalDate.now().isAfter(endDate)

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readSerializable() as LocalDate,
            source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(name)
        dest?.writeSerializable(startDate)
        dest?.writeSerializable(endDate)
        dest?.writeInt(weekRotations)
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<Timetable> = object : Parcelable.Creator<Timetable> {
            override fun createFromParcel(source: Parcel): Timetable = Timetable(source)
            override fun newArray(size: Int): Array<Timetable?> = arrayOfNulls(size)
        }

        @JvmStatic
        fun create(context: Context, timetableId: Int): Timetable? {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    TimetablesSchema.TABLE_NAME,
                    null,
                    "${TimetablesSchema._ID}=?",
                    arrayOf(timetableId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                return null
            }
            val timetable = Timetable(cursor)
            cursor.close()
            return timetable
        }

    }
}
