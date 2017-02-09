package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.schema.TimetablesSchema
import com.satsumasoftware.timetable.framework.Timetable

class TimetableUtils : DataUtils<Timetable> {

    override val tableName = TimetablesSchema.TABLE_NAME

    override val itemIdCol = TimetablesSchema._ID

    override fun createFromCursor(cursor: Cursor) = Timetable.from(cursor)

    override fun propertiesAsContentValues(item: Timetable): ContentValues {
        val values = ContentValues()
        with(values) {
            put(TimetablesSchema._ID, item.id)
            put(TimetablesSchema.COL_NAME, item.name)
            put(TimetablesSchema.COL_START_DATE_DAY_OF_MONTH, item.startDate.dayOfMonth)
            put(TimetablesSchema.COL_START_DATE_MONTH, item.startDate.monthValue)
            put(TimetablesSchema.COL_START_DATE_YEAR, item.startDate.year)
            put(TimetablesSchema.COL_END_DATE_DAY_OF_MONTH, item.endDate.dayOfMonth)
            put(TimetablesSchema.COL_END_DATE_MONTH, item.endDate.monthValue)
            put(TimetablesSchema.COL_END_DATE_YEAR, item.endDate.year)
            put(TimetablesSchema.COL_WEEK_ROTATIONS, item.weekRotations)
        }
        return values
    }

    override fun replaceItem(context: Context, oldItemId: Int, newItem: Timetable) {
        super.replaceItem(context, oldItemId, newItem)

        // Refresh alarms in case start/end dates have changed
        val application = context.applicationContext as TimetableApplication
        application.refreshAlarms(context)
    }
}
