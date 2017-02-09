package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.db.schema.AssignmentsSchema
import com.satsumasoftware.timetable.db.schema.ClassesSchema
import com.satsumasoftware.timetable.framework.Class

class ClassUtils : TimetableItemUtils<Class> {

    override val tableName = ClassesSchema.TABLE_NAME

    override val itemIdCol = ClassesSchema._ID

    override val timetableIdCol = ClassesSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Class.from(cursor)

    override fun propertiesAsContentValues(item: Class): ContentValues {
        val values = ContentValues()
        with(values) {
            put(ClassesSchema._ID, item.id)
            put(ClassesSchema.COL_TIMETABLE_ID, item.timetableId)
            put(ClassesSchema.COL_SUBJECT_ID, item.subjectId)
            put(ClassesSchema.COL_MODULE_NAME, item.moduleName)
            put(ClassesSchema.COL_START_DATE_DAY_OF_MONTH, item.startDate.dayOfMonth)
            put(ClassesSchema.COL_START_DATE_MONTH, item.startDate.monthValue)
            put(ClassesSchema.COL_START_DATE_YEAR, item.startDate.year)
            put(ClassesSchema.COL_END_DATE_DAY_OF_MONTH, item.endDate.dayOfMonth)
            put(ClassesSchema.COL_END_DATE_MONTH, item.endDate.monthValue)
            put(ClassesSchema.COL_END_DATE_YEAR, item.endDate.year)
        }
        return values
    }

    override fun deleteItemWithReferences(context: Context, itemId: Int) {
        super.deleteItemWithReferences(context, itemId)

        val assignmentsQuery = Query.Builder()
                .addFilter(Filters.equal(AssignmentsSchema.COL_CLASS_ID, itemId.toString()))
                .build()

        val assignmentUtils = AssignmentUtils()
        for (assignment in assignmentUtils.getAllItems(context, assignmentsQuery)) {
            assignmentUtils.deleteItem(context, assignment.id)
        }

        for (classDetail in ClassDetailUtils.getClassDetailsForClass(context, itemId)) {
            ClassDetailUtils().deleteItemWithReferences(context, classDetail.id)
        }
    }

}
