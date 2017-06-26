package co.timetableapp.model

import android.content.Context
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * This should be implemented by classes that can be shown on the 'Agenda' part of the UI.
 */
interface AgendaItem : Comparable<AgendaItem> {

    /**
     * @return the title for the Agenda item
     */
    fun getDisplayedTitle(): String

    /**
     * @return  the related [Subject] of the Agenda item. If the item does not have a related
     *          subject, then this returns null.
     */
    fun getRelatedSubject(context: Context): Subject?

    /**
     * @return  the date and time of the item. If there is no time, then [LocalTime.MIDNIGHT] should
     *          be used as the time. This is used to sort Agenda items by date.
     */
    fun getDateTime(): LocalDateTime

    override fun compareTo(other: AgendaItem) = getDateTime().compareTo(other.getDateTime())

    /**
     * Defines a sorting order for Agenda items, first being sorted in reverse by date (so that when
     * viewing past assignments, the most recent is shown first), then lexicographically.
     */
    class ReverseDateComparator : Comparator<AgendaItem> {

        override fun compare(o1: AgendaItem?, o2: AgendaItem?): Int {
            val dateComparison = o2!!.getDateTime().compareTo(o1!!.getDateTime())
            return if (dateComparison == 0) {
                o1.getDisplayedTitle().compareTo(o2.getDisplayedTitle())
            } else {
                dateComparison
            }
        }
    }

}
