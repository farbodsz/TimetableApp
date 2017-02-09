package com.satsumasoftware.timetable.db

import android.app.Application
import android.content.Context
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.framework.TimetableItem
import java.util.*

abstract class TimetableItemHandler<T : TimetableItem>(context: Context) : DataHandler<T>(context) {

    abstract val timetableIdCol: String

    fun getItems(application: Application): ArrayList<T> {
        val timetable = (application as TimetableApplication).currentTimetable!!

        val query = Query.Builder()
                .addFilter(Filters.equal(timetableIdCol, timetable.id.toString()))
                .build()

        return getAllItems(query)
    }

}
