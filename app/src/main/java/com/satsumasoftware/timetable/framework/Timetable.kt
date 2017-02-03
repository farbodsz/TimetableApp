package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.TimetablesSchema
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * A timetable should be used by the user to represent an academic year.
 *
 * A Timetable stores an integer identifier (id), a name, start and end dates, and the number of
 * week rotations it uses (whether classes are the same each week, or vary depending on the week).
 *
 * Other components of this app such as [classes][Class], [exams][Exam] and
 * [assignments][Assignment] store a timetable id which is used by the database to link them to
 * their timetable. It can be considered that a timetable has (i.e. is linked to) classes, exams,
 * and assignments, exactly like how one would have different classes, exams, and assignments for
 * each academic year.
 *
 * @property id an integer identifier for this timetable
 * @property name the name of the timetable. This could be the name of the academic year (e.g.
 *      "Year 11", "9th Grade", "2016") or something else (e.g. "Friend's Timetable", "Evening
 *      classes").
 * @property startDate the first day this timetable is applicable for
 * @property endDate the last day this timetable is applicable for
 */
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

    companion object {

        /**
         * Constructs a [Timetable] using column values from the cursor provided
         *
         * @param cursor a query of the timetables table
         * @see [TimetablesSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Timetable {
            val startDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_DAY_OF_MONTH)))
            val endDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_DAY_OF_MONTH)))

            return Timetable(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema._ID)),
                    cursor.getString(cursor.getColumnIndex(TimetablesSchema.COL_NAME)),
                    startDate,
                    endDate,
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_WEEK_ROTATIONS)))
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
            val timetable = Timetable.from(cursor)
            cursor.close()
            return timetable
        }

        @Suppress("unused") @JvmField val CREATOR: Parcelable.Creator<Timetable> =
                object : Parcelable.Creator<Timetable> {
                    override fun createFromParcel(source: Parcel): Timetable = Timetable(source)
                    override fun newArray(size: Int): Array<Timetable?> = arrayOfNulls(size)
                }
    }

    fun hasName() = name.trim().isNotEmpty()

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
    
}
