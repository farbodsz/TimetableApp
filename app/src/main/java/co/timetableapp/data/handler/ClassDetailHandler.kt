package co.timetableapp.data.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.ClassDetailsSchema
import co.timetableapp.framework.ClassDetail
import java.util.*

class ClassDetailHandler(context: Context) : DataHandler<ClassDetail>(context) {

    override val tableName = ClassDetailsSchema.TABLE_NAME

    override val itemIdCol = ClassDetailsSchema._ID

    override fun createFromCursor(cursor: Cursor) = ClassDetail.from(cursor)

    override fun createFromId(id: Int) = ClassDetail.create(context, id)

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

        ClassTimeHandler.getClassTimesForDetail(context, itemId).forEach {
            ClassHandler(context).deleteItemWithReferences(it.id)
        }
    }

    companion object {
        @JvmStatic
        fun getClassDetailsForClass(context: Context, classId: Int): ArrayList<ClassDetail> {
            val query = Query.Builder()
                    .addFilter(Filters.equal(ClassDetailsSchema.COL_CLASS_ID, classId.toString()))
                    .build()
            return ClassDetailHandler(context).getAllItems(query)
        }
    }

}
