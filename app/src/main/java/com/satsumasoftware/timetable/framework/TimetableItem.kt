package com.satsumasoftware.timetable.framework

/**
 * Data classes that are part of a timetable should implement this.
 */
interface TimetableItem : BaseItem {


    /**
     * The integer identifier for the ([Timetable]) this data item is part of.
     *
     * This is used to filter the items for the user's currently selected timetable.
     */
    val timetableId: Int

}
