package com.satsumasoftware.timetable.framework

import android.os.Parcel
import android.os.Parcelable
import org.threeten.bp.LocalDateTime

/**
 * Represents an event that is not part of the usual schedule of the user.
 * For example, a meeting with a tutor, a sporting event, etc.
 *
 * @property id an integer identifier for the event
 * @property timetableId the ID of the linked [Timetable]
 * @property title the event's title
 * @property detail additional notes and details for the event
 * @property startTime the starting time and date
 * @property endTime the ending time and date
 */
class Event(val id: Int, val timetableId: Int, val title: String, val detail: String,
            val startTime: LocalDateTime, val endTime: LocalDateTime) : Parcelable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Event> = object : Parcelable.Creator<Event> {
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

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeString(title)
        dest?.writeString(detail)
        dest?.writeSerializable(startTime)
        dest?.writeSerializable(endTime)
    }

}
