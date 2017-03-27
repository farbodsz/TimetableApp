package co.timetableapp.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.schema.TermsSchema
import org.threeten.bp.LocalDate

/**
 * Represents a term (or semester) in a student's timetable.
 *
 * A `Term` does not have any links with classes, assignments, exams, or any other model except
 * from the [Timetable] to which it belongs to. This means that [Class]es must set their own start
 * and end dates.
 *
 * @property timetableId the identifier of the [Timetable] this term belongs to
 * @property name the name for this term (e.g. First Semester, Summer Term, etc.)
 * @property startDate the start date of this term
 * @property endDate the end date of this term
 */
class Term(override val id: Int, override val timetableId: Int, val name: String,
           val startDate: LocalDate, val endDate: LocalDate) : TimetableItem {

    init {
        if (startDate.isAfter(endDate)) {
            throw IllegalArgumentException("the start date cannot be after the end date")
        }
    }

    companion object {

        /**
         * Constructs a [Term] using column values from the cursor provided
         *
         * @param cursor a query of the terms table
         * @see [TermsSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Term {
            val startDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_START_DATE_DAY_OF_MONTH)))
            val endDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_END_DATE_DAY_OF_MONTH)))

            return Term(
                    cursor.getInt(cursor.getColumnIndex(TermsSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_TIMETABLE_ID)),
                    cursor.getString(cursor.getColumnIndex(TermsSchema.COL_NAME)),
                    startDate,
                    endDate)
        }

        @JvmStatic
        fun create(context: Context, termId: Int): Term? {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    TermsSchema.TABLE_NAME,
                    null,
                    "${TermsSchema._ID}=?",
                    arrayOf(termId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                return null
            }
            val term = Term.from(cursor)
            cursor.close()
            return term
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Term> = object : Parcelable.Creator<Term> {
            override fun createFromParcel(source: Parcel): Term = Term(source)
            override fun newArray(size: Int): Array<Term?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readSerializable() as LocalDate)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeString(name)
        dest?.writeSerializable(startDate)
        dest?.writeSerializable(endDate)
    }

}
