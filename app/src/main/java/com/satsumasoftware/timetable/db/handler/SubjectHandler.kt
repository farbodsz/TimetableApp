package com.satsumasoftware.timetable.db.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.db.schema.ClassesSchema
import com.satsumasoftware.timetable.db.schema.ExamsSchema
import com.satsumasoftware.timetable.db.schema.SubjectsSchema
import com.satsumasoftware.timetable.framework.Subject

class SubjectHandler(context: Context) : TimetableItemHandler<Subject>(context) {

    override val tableName = SubjectsSchema.TABLE_NAME

    override val itemIdCol = SubjectsSchema._ID

    override val timetableIdCol = SubjectsSchema.COL_TIMETABLE_ID

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

    override fun deleteItemWithReferences(itemId: Int) {
        super.deleteItemWithReferences(itemId)

        val classesQuery = Query.Builder()
                .addFilter(Filters.equal(ClassesSchema.COL_SUBJECT_ID, itemId.toString()))
                .build()

        val classUtils = ClassHandler(context)
        classUtils.getAllItems(classesQuery).forEach {
            classUtils.deleteItemWithReferences(it.id)
        }

        val examsQuery = Query.Builder()
                .addFilter(Filters.equal(ExamsSchema.COL_SUBJECT_ID, itemId.toString()))
                .build()

        val examUtils = ExamHandler(context)
        examUtils.getAllItems(examsQuery).forEach {
            examUtils.deleteItem(it.id)
        }
    }

}
