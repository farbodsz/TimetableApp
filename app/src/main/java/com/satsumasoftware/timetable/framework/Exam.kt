package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.ExamsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Represents an exam.
 *
 * @property id An integer identifier
 * @property timetableId The identifier of the associated timetable
 * @property subjectId The identifier of the subject the exam is linked with
 * @property moduleName An optional name for the module of the exam
 * @property date The date the exam takes place
 * @property startTime The start time of the exam
 * @property duration An integer storing how long the exam would last (in minutes)
 * @property seat An optional string value denoting the seat the candidate would be in for the exam
 * @property room An optional string value denoting the room the candidate would be in for the exam
 * @property resit A boolean value indicating whether or not the exam is a resit
 */
class Exam(val id: Int, val timetableId: Int, val subjectId: Int, val moduleName: String,
           val date: LocalDate, val startTime: LocalTime, val duration: Int, val seat: String,
           val room: String, val resit: Boolean) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ExamsSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_TIMETABLE_ID)),
            cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_SUBJECT_ID)),
            cursor.getString(cursor.getColumnIndex(ExamsSchema.COL_MODULE)),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_DATE_DAY_OF_MONTH))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_START_TIME_MINS))),
            cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_DURATION)),
            cursor.getString(cursor.getColumnIndex(ExamsSchema.COL_SEAT)),
            cursor.getString(cursor.getColumnIndex(ExamsSchema.COL_ROOM)),
            cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_IS_RESIT)) == 1)

    constructor(source: Parcel): this(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readSerializable() as LocalTime,
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readInt() == 1)

    fun hasModuleName() = moduleName.trim().isNotEmpty()

    fun hasSeat() = seat.trim().isNotEmpty()

    fun hasRoom() = room.trim().isNotEmpty()

    /**
     * @return a [LocalDateTime] object using the [date] and [startTime] of the exam
     */
    fun makeDateTimeObject() = LocalDateTime.of(date, startTime)!!

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeInt(subjectId)
        dest?.writeString(moduleName)
        dest?.writeSerializable(date)
        dest?.writeSerializable(startTime)
        dest?.writeInt(duration)
        dest?.writeString(seat)
        dest?.writeString(room)
        dest?.writeInt(if (resit) 1 else 0)
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<Exam> = object : Parcelable.Creator<Exam> {
            override fun createFromParcel(source: Parcel): Exam = Exam(source)
            override fun newArray(size: Int): Array<Exam?> = arrayOfNulls(size)
        }

        @JvmStatic
        fun create(context: Context, examId: Int): Exam? {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ExamsSchema.TABLE_NAME,
                    null,
                    "${ExamsSchema._ID}=?",
                    arrayOf(examId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                return null
            }
            val exam = Exam(cursor)
            cursor.close()
            return exam
        }

        /**
         * @return the displayed name for the exam, consisting of the subject name and exam module
         * name if it exists
         */
        @JvmStatic
        fun makeName(exam: Exam, subject: Subject) = if (exam.hasModuleName()) {
            "${subject.name}: ${exam.moduleName}"
        } else {
            subject.name
        }

    }
}
