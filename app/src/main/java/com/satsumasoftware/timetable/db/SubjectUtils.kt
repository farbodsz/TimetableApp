package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.database.Cursor
import com.satsumasoftware.timetable.db.schema.SubjectsSchema
import com.satsumasoftware.timetable.framework.Subject

class SubjectUtils : TimetableItemUtils<Subject> {

    override val tableName = SubjectsSchema.TABLE_NAME

    override val itemIdCol = SubjectsSchema._ID

    override fun createFromCursor(cursor: Cursor) = Subject.from(cursor)

    override fun propertiesAsContentValues(item: Subject): ContentValues {
        val values = ContentValues()
        with(values) {
            put(SubjectsSchema._ID, item.id)
            put(SubjectsSchema.COL_TIMETABLE_ID, item.timetableId)
            put(SubjectsSchema.COL_NAME, item.name)
            put(SubjectsSchema.COL_ABBREVIATION, item.abbreviation)
            put(SubjectsSchema.COL_COLOR_ID, item.colorId)
        }
        return values
    }

    override val timetableIdCol = SubjectsSchema.COL_TIMETABLE_ID

}
