package co.timetableapp.db.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import co.timetableapp.db.query.Filters
import co.timetableapp.db.query.Query
import co.timetableapp.db.schema.AssignmentsSchema
import co.timetableapp.db.schema.ClassesSchema
import co.timetableapp.framework.Class

class ClassHandler(context: Context) : TimetableItemHandler<Class>(context) {

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

    override fun deleteItemWithReferences(itemId: Int) {
        super.deleteItemWithReferences(itemId)

        val assignmentsQuery = Query.Builder()
                .addFilter(Filters.equal(AssignmentsSchema.COL_CLASS_ID, itemId.toString()))
                .build()

        val assignmentUtils = AssignmentHandler(context)
        assignmentUtils.getAllItems(assignmentsQuery).forEach {
            assignmentUtils.deleteItem(it.id)
        }

        ClassDetailHandler.getClassDetailsForClass(context, itemId).forEach {
            ClassDetailHandler(context).deleteItemWithReferences(it.id)
        }
    }

}
