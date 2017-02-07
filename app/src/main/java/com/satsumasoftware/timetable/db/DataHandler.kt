package com.satsumasoftware.timetable.db

import android.database.Cursor
import com.satsumasoftware.timetable.framework.BaseItem

interface DataHandler<out T : BaseItem> {

    fun createFromCursor(cursor: Cursor): T

}
