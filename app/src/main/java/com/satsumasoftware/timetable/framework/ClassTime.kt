package com.satsumasoftware.timetable.framework

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.schema.ClassTimesSchema
import com.satsumasoftware.timetable.util.PrefUtils
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

/**
 * A collection of data relating to a time that the class takes place.
 *
 * Data about when the class takes place can be organized into `ClassTime` objects. Each of these
 * has properties for a day of the week, which weeks it takes place, and of course, start and end
 * times.
 *
 * So each `ClassTime` only includes data for one occurrence of the class (e.g. 12:00 to 13:00 on
 * Mondays on Week 2s).
 *
 * Note that a `ClassTime` is linked to a [ClassDetail] and not a [Class] so that we know the times
 * for each different class detail (e.g. when the student gets taught by teacher A and when they
 * get taught by teacher B).
 *
 * @property id the identifier for this class time
 * @property timetableId the identifier of the associated [Timetable]. We include this in our
 *      `ClassTime` for when we want to find a list of times in one particular timetable.
 * @property classDetailId the identifier of the associated [ClassDetail]
 * @property day a day of the week (Monday to Sunday) that the class takes place
 * @property weekNumber the number of a week rotation where the class takes place
 * @property startTime a start time of the class
 * @property endTime an end time of the class
 */
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

    /**
     * @return the string to be displayed indicating the week rotation (e.g. Week 1, Week C).
     *
     * @see Companion.getWeekText
     */
    fun getWeekText(activity: Activity) = Companion.getWeekText(activity, weekNumber)

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

        @Suppress("unused") @JvmField val CREATOR: Parcelable.Creator<ClassTime> =
                object : Parcelable.Creator<ClassTime> {
                    override fun createFromParcel(source: Parcel): ClassTime = ClassTime(source)
                    override fun newArray(size: Int): Array<ClassTime?> = arrayOfNulls(size)
                }

        @JvmStatic
        fun create(context: Context, classTimeId: Int): ClassTime {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassTimesSchema.TABLE_NAME,
                    null,
                    "${ClassTimesSchema._ID}=?",
                    arrayOf(classTimeId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            val classTime = ClassTime(cursor)
            cursor.close()
            return classTime
        }

        /**
         * @return the string to be displayed indicating the week rotation (e.g. Week 1, Week C).
         */
        @JvmOverloads
        @JvmStatic
        fun getWeekText(activity: Activity, weekNumber: Int, fullText: Boolean = true): String {
            val timetable = (activity.application as TimetableApplication).currentTimetable!!
            if (timetable.hasFixedScheduling()) {
                return ""
            } else {
                val weekChar = if (PrefUtils.displayWeeksAsLetters(activity)) {
                    when(weekNumber) {
                        1 -> "A"
                        2 -> "B"
                        3 -> "C"
                        4 -> "D"
                        else -> throw IllegalArgumentException("invalid week number '$weekNumber'")
                    }
                } else {
                    weekNumber.toString()
                }
                return if (fullText) activity.getString(R.string.week_item, weekChar) else weekChar
            }
        }
    }
}
