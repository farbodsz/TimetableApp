package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.db.schema.ClassDetailsSchema
import com.satsumasoftware.timetable.framework.ClassDetail
import java.util.*

class ClassDetailUtils(context: Context) : DataUtils<ClassDetail>(context) {

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

    override fun deleteItemWithReferences(itemId: Int) {
        super.deleteItemWithReferences(itemId)

        for (classTime in ClassTimeUtils.getClassTimesForDetail(context, itemId)) {
            ClassUtils(context).deleteItemWithReferences(classTime.id)
        }
    }

    companion object {
        @JvmStatic
        fun getClassDetailsForClass(context: Context, classId: Int): ArrayList<ClassDetail> {
            val query = Query.Builder()
                    .addFilter(Filters.equal(ClassDetailsSchema.COL_CLASS_ID, classId.toString()))
                    .build()
            return ClassDetailUtils(context).getAllItems(query)
        }
    }

}
