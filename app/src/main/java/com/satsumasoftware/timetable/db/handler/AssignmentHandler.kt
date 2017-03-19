package com.satsumasoftware.timetable.db.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.db.schema.AssignmentsSchema
import com.satsumasoftware.timetable.framework.Assignment

class AssignmentHandler(context: Context) : TimetableItemHandler<Assignment>(context) {

    override val tableName = AssignmentsSchema.TABLE_NAME

    override val itemIdCol = AssignmentsSchema._ID

    override val timetableIdCol = AssignmentsSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Assignment.from(cursor)

    override fun createFromId(id: Int) = Assignment.create(context, id)

    override fun propertiesAsContentValues(item: Assignment): ContentValues {
        val values = ContentValues()
        with(values) {
            put(AssignmentsSchema._ID, item.id)
            put(AssignmentsSchema.COL_TIMETABLE_ID, item.timetableId)
            put(AssignmentsSchema.COL_CLASS_ID, item.classId)
            put(AssignmentsSchema.COL_TITLE, item.title)
            put(AssignmentsSchema.COL_DETAIL, item.detail)
            put(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH, item.dueDate.dayOfMonth)
            put(AssignmentsSchema.COL_DUE_DATE_MONTH, item.dueDate.monthValue)
            put(AssignmentsSchema.COL_DUE_DATE_YEAR, item.dueDate.year)
            put(AssignmentsSchema.COL_COMPLETION_PROGRESS, item.completionProgress)
        }
        return values
    }

}
