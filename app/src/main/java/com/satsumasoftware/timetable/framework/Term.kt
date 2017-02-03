package com.satsumasoftware.timetable.framework

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.TermsSchema
import org.threeten.bp.LocalDate

class Term(val id: Int, val timetableId: Int, val name: String, val startDate: LocalDate,
           val endDate: LocalDate) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(TermsSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_TIMETABLE_ID)),
            cursor.getString(cursor.getColumnIndex(TermsSchema.COL_NAME)),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_START_DATE_DAY_OF_MONTH))),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TermsSchema.COL_END_DATE_DAY_OF_MONTH))))

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

    companion object {
        @Suppress("unused") @JvmField val CREATOR: Parcelable.Creator<Term> =
                object : Parcelable.Creator<Term> {
                    override fun createFromParcel(source: Parcel): Term = Term(source)
                    override fun newArray(size: Int): Array<Term?> = arrayOfNulls(size)
                }
    }
}
