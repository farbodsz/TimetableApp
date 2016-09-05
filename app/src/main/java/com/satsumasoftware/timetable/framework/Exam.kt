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

    fun hasModuleName() = moduleName.trim().length != 0

    fun hasSeat() = seat.trim().length != 0

    fun hasRoom() = room.trim().length != 0

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

        @JvmStatic fun create(context: Context, examId: Int): Exam? {
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

        @JvmStatic fun makeName(exam: Exam, subject: Subject) = if (exam.hasModuleName()) {
            "${subject.name}: ${exam.moduleName}"
        } else {
            subject.name
        }

    }
}
