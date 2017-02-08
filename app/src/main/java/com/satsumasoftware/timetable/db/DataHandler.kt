package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.database.Cursor
import com.satsumasoftware.timetable.framework.BaseItem

interface DataHandler<T : BaseItem> {

    val tableName: String

    val itemIdCol: String

    fun createFromCursor(cursor: Cursor): T

    fun propertiesAsContentValues(item: T): ContentValues

}
