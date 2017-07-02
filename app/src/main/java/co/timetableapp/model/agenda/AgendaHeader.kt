package co.timetableapp.model.agenda

import android.content.Context

/**
 * Represent headers on lists of Agenda items.
 *
 * Headers can either be relative, meaning that their date period is dependent on the current day
 * (e.g. "Tomorrow", "Next Week"); or they can be custom, where their date period can be
 * any month (e.g. "January 2014", "May 2012").
 *
 * @see RelativeAgendaHeader
 * @see CustomAgendaHeader
 */
abstract class AgendaHeader : AgendaListItem {

    /**
     * @param context the activity context, used to get a string from a string resource if necessary
     *
     * @return the text displayed on the header
     */
    abstract fun getName(context: Context): String

    /**
     * Compares this header with another header, like [compareTo].
     *
     * This should be used instead of simply comparing the dates of headers from [getDateTime]
     * since often the dates for different headers can be the same; for example in relative headers,
     * "Tomorrow" could also be "Next Week".
     *
     * @return  a negative integer, zero, or a positive integer if the header is less than, equal
     *          to, or greater than the [other] header.
     */
    fun compareHeaders(other: AgendaHeader): Int {
        val datetimeComparison = getDateTime().compareTo(other.getDateTime())
        return if (datetimeComparison != 0) {
            datetimeComparison
        } else {
            getPriority().compareTo(other.getPriority())
        }
    }

    /**
     * @return  an integer denoting the "priority" of a header, used to determine the importance of
     *          headers where two headers have the same date. A higher number denotes a higher
     *          priority level.
     */
    abstract fun getPriority(): Int

    override fun isHeader() = true

}
