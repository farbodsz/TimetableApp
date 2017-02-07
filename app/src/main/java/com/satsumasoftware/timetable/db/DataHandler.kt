package com.satsumasoftware.timetable.db

import android.database.Cursor
import com.satsumasoftware.timetable.framework.BaseItem

interface DataHandler<out T : BaseItem> {

    val tableName: String

    val itemIdCol: String

    val timetableIdCol: String

    fun createFromCursor(cursor: Cursor): T

}
