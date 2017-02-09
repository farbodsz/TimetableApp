package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.database.Cursor
import com.satsumasoftware.timetable.db.schema.ClassTimesSchema
import com.satsumasoftware.timetable.framework.ClassTime

class ClassTimeUtils : TimetableItemUtils<ClassTime> {

    override val tableName = ClassTimesSchema.TABLE_NAME

    override val itemIdCol = ClassTimesSchema._ID

    override fun createFromCursor(cursor: Cursor) = ClassTime.from(cursor)

    override fun propertiesAsContentValues(item: ClassTime): ContentValues {
        val values = ContentValues()
        with(values) {
            put(ClassTimesSchema._ID, item.id)
            put(ClassTimesSchema.COL_TIMETABLE_ID, item.timetableId)
            put(ClassTimesSchema.COL_CLASS_DETAIL_ID, item.classDetailId)
            put(ClassTimesSchema.COL_DAY, item.day.value)
            put(ClassTimesSchema.COL_WEEK_NUMBER, item.weekNumber)
            put(ClassTimesSchema.COL_START_TIME_HRS, item.startTime.hour)
            put(ClassTimesSchema.COL_START_TIME_MINS, item.startTime.minute)
            put(ClassTimesSchema.COL_END_TIME_HRS, item.endTime.hour)
            put(ClassTimesSchema.COL_END_TIME_MINS, item.endTime.minute)
        }
        return values    }

    override val timetableIdCol = ClassTimesSchema.COL_TIMETABLE_ID

}
