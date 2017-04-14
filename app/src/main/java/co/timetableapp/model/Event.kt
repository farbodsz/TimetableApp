package co.timetableapp.model

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.schema.EventsSchema
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Represents an event that is not part of the usual schedule of the user.
 * For example, a meeting with a tutor, a sporting event, etc.
 *
 * @property title the event's title
 * @property detail additional notes and details for the event
 * @property startTime the starting time and date
 * @property endTime the ending time and date
 */
class Event(override val id: Int, override val timetableId: Int, val title: String,
            val detail: String, val startTime: LocalDateTime,
            val endTime: LocalDateTime) : TimetableItem, Parcelable, Comparable<Event> {

    companion object {

        /**
         * @see ReverseDateTimeComparator
         */
        @JvmField val COMPARATOR_REVERSE_DATE_TIME = ReverseDateTimeComparator()

        /**
         * Constructs an Event using column values from the cursor provided
         *
         * @param cursor a query of the events table
         * @see [EventsSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Event {
            val startDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_DATE_DAY_OF_MONTH)))
            val startTime = LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_START_TIME_MINS)))

            val endDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_DATE_DAY_OF_MONTH)))
            val endTime = LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_END_TIME_MINS)))

            return Event(
                    cursor.getInt(cursor.getColumnIndex(EventsSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(EventsSchema.COL_TIMETABLE_ID)),
                    cursor.getString(cursor.getColumnIndex(EventsSchema.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(EventsSchema.COL_DETAIL)),
                    LocalDateTime.of(startDate, startTime),
                    LocalDateTime.of(endDate, endTime))
        }

        @JvmStatic
        fun create(context: Context, eventId: Int): Event {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    EventsSchema.TABLE_NAME,
                    null,
                    "${EventsSchema._ID}=?",
                    arrayOf(eventId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            val event = from(cursor)
            cursor.close()
            return event
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
            override fun createFromParcel(source: Parcel): Event = Event(source)
            override fun newArray(size: Int): Array<Event?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readSerializable() as LocalDateTime,
            source.readSerializable() as LocalDateTime)

    override fun compareTo(other: Event) = startTime.compareTo(other.startTime)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeString(title)
        dest?.writeString(detail)
        dest?.writeSerializable(startTime)
        dest?.writeSerializable(endTime)
    }

    /**
     * Defines a sorting order for events, first being sorted in reverse by date and time (so that
     * when viewing past events, the most recent is shown first).
     */
    class ReverseDateTimeComparator : Comparator<Event> {

        override fun compare(o1: Event?, o2: Event?): Int {
            return o2!!.startTime.compareTo(o1!!.startTime)
        }
    }

}
