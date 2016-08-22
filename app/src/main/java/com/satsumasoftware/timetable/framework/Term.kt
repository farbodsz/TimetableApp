package com.satsumasoftware.timetable.framework

import android.database.Cursor
import com.satsumasoftware.timetable.db.TermsSchema
import org.threeten.bp.LocalDate

class Term(val id: Int, val timetableId: Int, val name: String, val startDate: LocalDate,
           val endDate: LocalDate) {

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

}
