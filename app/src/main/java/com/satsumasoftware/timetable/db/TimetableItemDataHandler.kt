package com.satsumasoftware.timetable.db

import com.satsumasoftware.timetable.framework.TimetableItem

interface TimetableItemDataHandler<T : TimetableItem> : DataHandler<T> {

    val timetableIdCol: String

}
