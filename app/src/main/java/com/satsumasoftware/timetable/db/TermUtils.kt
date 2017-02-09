package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.db.schema.TermsSchema
import com.satsumasoftware.timetable.framework.Term

class TermUtils(context: Context) : TimetableItemUtils<Term>(context) {

    override val tableName = TermsSchema.TABLE_NAME

    override val itemIdCol = TermsSchema._ID

    override val timetableIdCol = TermsSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Term.from(cursor)

    override fun propertiesAsContentValues(item: Term): ContentValues {
        val values = ContentValues()
        with(values) {
            put(TermsSchema._ID, item.id)
            put(TermsSchema.COL_TIMETABLE_ID, item.timetableId)
            put(TermsSchema.COL_NAME, item.name)
            put(TermsSchema.COL_START_DATE_DAY_OF_MONTH, item.startDate.dayOfMonth)
            put(TermsSchema.COL_START_DATE_MONTH, item.startDate.monthValue)
            put(TermsSchema.COL_START_DATE_YEAR, item.startDate.year)
            put(TermsSchema.COL_END_DATE_DAY_OF_MONTH, item.endDate.dayOfMonth)
            put(TermsSchema.COL_END_DATE_MONTH, item.endDate.monthValue)
            put(TermsSchema.COL_END_DATE_YEAR, item.endDate.year)
        }
        return values
    }

}
