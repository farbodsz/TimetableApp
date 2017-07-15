/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.model

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.R
import co.timetableapp.TimetableApplication
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.data.schema.ClassTimesSchema
import co.timetableapp.util.PrefUtils
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
 * @property classDetailId  the id of the associated [ClassDetail]
 * @property day            the day of the week (Monday to Sunday) that the class takes place
 * @property weekNumber     the number of a week rotation when the class takes place
 * @property startTime      the time the class starts
 * @property endTime        the time the class ends
 */
data class ClassTime(
        override val id: Int,
        override val timetableId: Int,
        val classDetailId: Int,
        val day: DayOfWeek,
        val weekNumber: Int,
        val startTime: LocalTime,
        val endTime: LocalTime
) : TimetableItem, Comparable<ClassTime> {

    init {
        if (startTime.isAfter(endTime)) {
            throw IllegalArgumentException("the start time cannot be after the end time")
        }
    }

    companion object {

        /**
         * Constructs a [ClassTime] using column values from the cursor provided
         *
         * @param cursor a query of the class times table
         * @see [ClassTimesSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): ClassTime {
            val dayOfWeek =
                    DayOfWeek.of(cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_DAY)))

            val startTime = LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_START_TIME_MINS)))

            val endTime = LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_END_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_END_TIME_MINS)))

            return ClassTime(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_TIMETABLE_ID)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_CLASS_DETAIL_ID)),
                    dayOfWeek,
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_WEEK_NUMBER)),
                    startTime,
                    endTime)
        }

        /**
         * Creates a [ClassTime] from the [classTimeId] and corresponding data in the database.
         *
         * @throws DataNotFoundException if the database query returns no results
         * @see from
         */
        @JvmStatic
        @Throws(DataNotFoundException::class)
        fun create(context: Context, classTimeId: Int): ClassTime {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ClassTimesSchema.TABLE_NAME,
                    null,
                    "${ClassTimesSchema._ID}=?",
                    arrayOf(classTimeId.toString()),
                    null, null, null)

            if (cursor.count == 0) {
                cursor.close()
                throw DataNotFoundException(this::class.java, classTimeId)
            }

            cursor.moveToFirst()
            val classTime = ClassTime.from(cursor)
            cursor.close()
            return classTime
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<ClassTime> = object : Parcelable.Creator<ClassTime> {
            override fun createFromParcel(source: Parcel): ClassTime = ClassTime(source)
            override fun newArray(size: Int): Array<ClassTime?> = arrayOfNulls(size)
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
                val weekChar = if (PrefUtils.isWeekRotationShownWithNumbers(activity)) {
                    weekNumber.toString()
                } else {
                    when(weekNumber) {
                        1 -> "A"
                        2 -> "B"
                        3 -> "C"
                        4 -> "D"
                        else -> throw IllegalArgumentException("invalid week number '$weekNumber'")
                    }
                }
                return if (fullText) activity.getString(R.string.week_item, weekChar) else weekChar
            }
        }
    }

    /**
     * @return the string to be displayed indicating the week rotation (e.g. Week 1, Week C).
     *
     * @see Companion.getWeekText
     */
    fun getWeekText(activity: Activity) = Companion.getWeekText(activity, weekNumber)

    override fun compareTo(other: ClassTime): Int {
        // Sort by day, then by time
        val dayComparison = day.compareTo(other.day)
        return if (dayComparison != 0) {
            dayComparison
        } else {
            startTime.compareTo(other.startTime)
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

    /**
     * Defines a sorting order for [class times][ClassTime] so that they are sorted by start time,
     * then end time, then days, then week numbers.
     */
    class TimeComparator : Comparator<ClassTime> {

        override fun compare(o1: ClassTime?, o2: ClassTime?): Int {
            val startTimeComparison = o1!!.startTime.compareTo(o2!!.startTime)
            if (startTimeComparison != 0) {
                return startTimeComparison
            }

            val endTimeComparison = o1.endTime.compareTo(o2.endTime)
            if (endTimeComparison != 0) {
                return endTimeComparison
            }

            val dayComparison = o1.day.compareTo(o2.day)
            if (dayComparison != 0) {
                return dayComparison
            }

            return o1.weekNumber - o2.weekNumber
        }
    }

}
