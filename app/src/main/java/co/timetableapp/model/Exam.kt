package co.timetableapp.model

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.data.schema.ExamsSchema
import co.timetableapp.model.agenda.AgendaItem
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Represents an exam.
 *
 * @property subjectId  the identifier of the [Subject] this exam is linked with
 * @property moduleName an optional name for the module of this exam
 * @property date       the date of this exam
 * @property startTime  the start time of this exam
 * @property duration   an integer indicating how long this exam would last, in minutes
 * @property seat       the seat the candidate would be in for this exam. This can be blank.
 * @property room       the room the candidate would be in for this exam. This can be blank.
 * @property resit      indicates whether or not the exam is a resit
 * @property notes      additional details the student may want to note. This can be blank.
 */
data class Exam(
        override val id: Int,
        override val timetableId: Int,
        val subjectId: Int,
        val moduleName: String,
        val date: LocalDate,
        val startTime: LocalTime,
        val duration: Int,
        val seat: String,
        val room: String,
        val resit: Boolean,
        val notes: String
) : TimetableItem, AgendaItem {

    companion object {

        /**
         * Constructs an [Exam] using column values from the cursor provided
         *
         * @param cursor a query of the exams table
         * @see [ExamsSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Exam {
            val date = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_DATE_DAY_OF_MONTH)))
            val time = LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_START_TIME_MINS)))

            val isResit = cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_IS_RESIT)) == 1

            return Exam(cursor.getInt(cursor.getColumnIndex(ExamsSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_TIMETABLE_ID)),
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_SUBJECT_ID)),
                    cursor.getString(cursor.getColumnIndex(ExamsSchema.COL_MODULE)),
                    date,
                    time,
                    cursor.getInt(cursor.getColumnIndex(ExamsSchema.COL_DURATION)),
                    cursor.getString(cursor.getColumnIndex(ExamsSchema.COL_SEAT)),
                    cursor.getString(cursor.getColumnIndex(ExamsSchema.COL_ROOM)),
                    isResit,
                    cursor.getString(cursor.getColumnIndex(ExamsSchema.COL_NOTES)))
        }

        /**
         * Creates an [Exam] from the [examId] and corresponding data in the database.
         *
         * @throws DataNotFoundException if the database query returns no results
         * @see from
         */
        @JvmStatic
        @Throws(DataNotFoundException::class)
        fun create(context: Context, examId: Int): Exam {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    ExamsSchema.TABLE_NAME,
                    null,
                    "${ExamsSchema._ID}=?",
                    arrayOf(examId.toString()),
                    null, null, null)

            if (cursor.count == 0) {
                cursor.close()
                throw DataNotFoundException(this::class.java, examId)
            }

            cursor.moveToFirst()
            val exam = Exam.from(cursor)
            cursor.close()
            return exam
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Exam> = object : Parcelable.Creator<Exam> {
            override fun createFromParcel(source: Parcel): Exam = Exam(source)
            override fun newArray(size: Int): Array<Exam?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readSerializable() as LocalTime,
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readInt() == 1,
            source.readString())

    fun hasModuleName() = moduleName.trim().isNotEmpty()

    fun hasSeat() = seat.trim().isNotEmpty()

    fun hasRoom() = room.trim().isNotEmpty()

    /**
     * @return the displayed name for the exam, consisting of the subject name and exam module
     * name if it exists
     */
    fun makeName(subject: Subject) = if (hasModuleName()) {
        "${subject.name}: $moduleName"
    } else {
        subject.name
    }

    /**
     * @return a location string consisting of the seat and room texts
     */
    fun formatLocationText(): String {
        val stringBuilder = StringBuilder()

        if (hasSeat()) {
            stringBuilder.append(seat)

            if (hasRoom()) stringBuilder.append(" \u2022 ")
        }

        if (hasRoom()) {
            stringBuilder.append(room)
        }

        return stringBuilder.toString()
    }

    override fun getDisplayedTitle() = moduleName

    override fun getRelatedSubject(context: Context) = Subject.create(context, subjectId)

    override fun getDateTime() = LocalDateTime.of(date, startTime)!!

    override fun isInPast() = getDateTime().isBefore(LocalDateTime.now())

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
        dest?.writeString(notes)
    }

}
