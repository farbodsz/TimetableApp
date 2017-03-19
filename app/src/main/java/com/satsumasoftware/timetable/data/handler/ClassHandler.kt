package com.satsumasoftware.timetable.data.handler

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.data.query.Filters
import com.satsumasoftware.timetable.data.query.Query
import com.satsumasoftware.timetable.data.schema.AssignmentsSchema
import com.satsumasoftware.timetable.data.schema.ClassesSchema
import com.satsumasoftware.timetable.framework.Class

class ClassHandler(context: Context) : TimetableItemHandler<Class>(context) {

    override val tableName = ClassesSchema.TABLE_NAME

    override val itemIdCol = ClassesSchema._ID

    override val timetableIdCol = ClassesSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Class.from(cursor)

    override fun createFromId(id: Int) = Class.create(context, id)

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

    /**
     * @return a list of filtered classes based on whether they are a 'current' class (i.e. today's
     * date falls between the start and end dates of the class).
     *
     * @see Class.isCurrent
     */
    @JvmOverloads
    fun getCurrentClasses(application: Application, showAll: Boolean = false): ArrayList<Class> {
        if (showAll) {
            return getItems(application)
        }

        // TODO: use a more efficient filtering method
        val currentClasses = ArrayList<Class>()
        getItems(application).forEach {
            if (it.isCurrent()) {
                currentClasses.add(it)
            }
        }
        return currentClasses
    }

}
