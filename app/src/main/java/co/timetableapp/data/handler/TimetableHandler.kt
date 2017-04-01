package co.timetableapp.data.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import co.timetableapp.TimetableApplication
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.SubjectsSchema
import co.timetableapp.data.schema.TermsSchema
import co.timetableapp.data.schema.TimetablesSchema
import co.timetableapp.model.Timetable
import co.timetableapp.util.NotificationUtils

class TimetableHandler(context: Context) : DataHandler<Timetable>(context) {

    override val tableName = TimetablesSchema.TABLE_NAME

    override val itemIdCol = TimetablesSchema._ID

    override fun createFromCursor(cursor: Cursor) = Timetable.from(cursor)

    override fun createFromId(id: Int) = Timetable.create(context, id)

    override fun propertiesAsContentValues(item: Timetable): ContentValues {
        val values = ContentValues()
        with(values) {
            put(TimetablesSchema._ID, item.id)
            put(TimetablesSchema.COL_NAME, item.name)
            put(TimetablesSchema.COL_START_DATE_DAY_OF_MONTH, item.startDate.dayOfMonth)
            put(TimetablesSchema.COL_START_DATE_MONTH, item.startDate.monthValue)
            put(TimetablesSchema.COL_START_DATE_YEAR, item.startDate.year)
            put(TimetablesSchema.COL_END_DATE_DAY_OF_MONTH, item.endDate.dayOfMonth)
            put(TimetablesSchema.COL_END_DATE_MONTH, item.endDate.monthValue)
            put(TimetablesSchema.COL_END_DATE_YEAR, item.endDate.year)
            put(TimetablesSchema.COL_WEEK_ROTATIONS, item.weekRotations)
        }
        return values
    }

    override fun replaceItem(oldItemId: Int, newItem: Timetable) {
        super.replaceItem(oldItemId, newItem)

        // Refresh alarms in case start/end dates have changed
        val application = context.applicationContext as TimetableApplication
        NotificationUtils.refreshAlarms(context, application)
    }

    override fun deleteItemWithReferences(itemId: Int) {
        super.deleteItemWithReferences(itemId)

        // Note that we only need to delete subjects, terms and their references since classes,
        // assignments, exams, and everything else are linked to subjects.

        val subjectsQuery = Query.Builder()
                .addFilter(Filters.equal(SubjectsSchema.COL_TIMETABLE_ID, itemId.toString()))
                .build()

        val subjectUtils = SubjectHandler(context)
        subjectUtils.getAllItems(subjectsQuery).forEach {
            subjectUtils.deleteItemWithReferences(it.id)
        }

        val termsQuery = Query.Builder()
                .addFilter(Filters.equal(TermsSchema.COL_TIMETABLE_ID, itemId.toString()))
                .build()

        val termUtils = TermHandler(context)
        termUtils.getAllItems(termsQuery).forEach {
            termUtils.deleteItem(it.id)
        }
    }

}
