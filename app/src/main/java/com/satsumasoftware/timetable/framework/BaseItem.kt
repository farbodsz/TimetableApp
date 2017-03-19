package com.satsumasoftware.timetable.framework

import android.os.Parcelable

/**
 * All data model classes should implement this
 */
interface BaseItem : Parcelable {

    /**
     * The integer identifier used to represent the data item in the database.
     */
    val id: Int

}
