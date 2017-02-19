package co.timetableapp.db.handler

import android.app.Application
import android.content.Context
import co.timetableapp.TimetableApplication
import co.timetableapp.db.query.Filters
import co.timetableapp.db.query.Query
import co.timetableapp.framework.TimetableItem
import java.util.*

/**
 * This abstract data handler provides additional functionality only available for types that are
 * subclasses of [TimetableItem].
 */
abstract class TimetableItemHandler<T : TimetableItem>(context: Context) : DataHandler<T>(context) {

    /**
     * The column name of the column that contains the timetable ids in the table.
     */
    abstract val timetableIdCol: String

    /**
     * @return a list of all items of type [T] that belong to the current timetable.
     */
    fun getItems(application: Application): ArrayList<T> {
        val timetable = (application as TimetableApplication).currentTimetable!!

        val query = Query.Builder()
                .addFilter(Filters.equal(timetableIdCol, timetable.id.toString()))
                .build()

        return getAllItems(query)
    }

}
