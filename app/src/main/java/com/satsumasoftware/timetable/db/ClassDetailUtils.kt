package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.database.Cursor
import com.satsumasoftware.timetable.db.schema.ClassDetailsSchema
import com.satsumasoftware.timetable.framework.ClassDetail

class ClassDetailUtils : DataUtils<ClassDetail> {

    override val tableName = ClassDetailsSchema.TABLE_NAME

    override val itemIdCol = ClassDetailsSchema._ID

    override fun createFromCursor(cursor: Cursor) = ClassDetail.from(cursor)

    override fun propertiesAsContentValues(item: ClassDetail): ContentValues {
        val values = ContentValues()
        with(values) {
            put(ClassDetailsSchema._ID, item.id)
            put(ClassDetailsSchema.COL_CLASS_ID, item.classId)
            put(ClassDetailsSchema.COL_ROOM, item.room)
            put(ClassDetailsSchema.COL_BUILDING, item.building)
            put(ClassDetailsSchema.COL_TEACHER, item.teacher)
        }
        return values
    }
}
