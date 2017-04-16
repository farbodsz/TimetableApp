package co.timetableapp.model

import android.os.Parcelable

/**
 * All data model classes should implement this
 */
interface BaseItem : Parcelable {

    /**
     * The integer identifier used to represent the data item in the database.
     */
    val id: Int

    /**
     * Specifies a sorting order based on the integer identifiers of a [BaseItem].
     * They would be sorted from lowest id to highest id.
     *
     * @see id
     */
    class ItemIdComparator : Comparator<BaseItem> {

        override fun compare(o1: BaseItem?, o2: BaseItem?) = o1!!.id.compareTo(o2!!.id)
    }

}
