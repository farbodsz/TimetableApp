package co.timetableapp.model

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.data.schema.EventsSchema
import co.timetableapp.model.agenda.AgendaItem
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Represents an event that is not part of the usual schedule of the user.
 * For example, a meeting with a tutor, a sporting event, etc.
 *
 * @property title          the event's title
 * @property notes          additional notes and details for the event
 * @property startDateTime  the starting time and date
 * @property endDateTime    the ending time and date
 */
data class Event(
        override val id: Int,
        override val timetableId: Int,
        val title: String,
        val notes: String,
        val startDateTime: LocalDateTime,
        val endDateTime: LocalDateTime
) : TimetableItem, AgendaItem {

    companion object {

        /**
         * The color to use when displaying events in lists.
         */
        @JvmField val DEFAULT_COLOR = Color(19) // blue-grey

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

        /**
         * Creates an [Event] from the [eventId] and corresponding data in the database.
         *
         * @throws DataNotFoundException if the database query returns no results
         * @see from
         */
        @JvmStatic
        @Throws(DataNotFoundException::class)
        fun create(context: Context, eventId: Int): Event {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    EventsSchema.TABLE_NAME,
                    null,
                    "${EventsSchema._ID}=?",
                    arrayOf(eventId.toString()),
                    null, null, null)

            if (cursor.count == 0) {
                cursor.close()
                throw DataNotFoundException(this::class.java, eventId)
            }

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

    fun hasDifferentStartEndDates() = startDateTime.toLocalDate() != endDateTime.toLocalDate()

    fun hasNotes() = notes.isNotEmpty()

    override fun getDisplayedTitle() = title

    // TODO related subjects feature
    override fun getRelatedSubject(context: Context) = null

    override fun getDateTime() = startDateTime

    override fun isInPast() = startDateTime.isBefore(LocalDateTime.now())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeString(title)
        dest?.writeString(notes)
        dest?.writeSerializable(startDateTime)
        dest?.writeSerializable(endDateTime)
    }

}
