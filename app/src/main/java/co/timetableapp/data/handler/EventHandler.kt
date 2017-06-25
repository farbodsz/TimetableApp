package co.timetableapp.data.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import co.timetableapp.data.schema.EventsSchema
import co.timetableapp.model.Event
import co.timetableapp.receiver.AlarmReceiver
import co.timetableapp.util.DateUtils
import co.timetableapp.util.PrefUtils

class EventHandler(context: Context) : TimetableItemHandler<Event>(context) {

    override val tableName = EventsSchema.TABLE_NAME

    override val itemIdCol = EventsSchema._ID

    override val timetableIdCol = EventsSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Event.from(cursor)

    override fun createFromId(id: Int) = Event.create(context, id)

    override fun propertiesAsContentValues(item: Event): ContentValues {
        val values = ContentValues()
        with(values) {
            put(EventsSchema._ID, item.id)
            put(EventsSchema.COL_TIMETABLE_ID, item.timetableId)
            put(EventsSchema.COL_TITLE, item.title)
            put(EventsSchema.COL_DETAIL, item.notes)
            put(EventsSchema.COL_START_DATE_DAY_OF_MONTH, item.startDateTime.dayOfMonth)
            put(EventsSchema.COL_START_DATE_MONTH, item.startDateTime.monthValue)
            put(EventsSchema.COL_START_DATE_YEAR, item.startDateTime.year)
            put(EventsSchema.COL_START_TIME_HRS, item.startDateTime.hour)
            put(EventsSchema.COL_START_TIME_MINS, item.startDateTime.minute)
            put(EventsSchema.COL_END_DATE_DAY_OF_MONTH, item.endDateTime.dayOfMonth)
            put(EventsSchema.COL_END_DATE_MONTH, item.endDateTime.monthValue)
            put(EventsSchema.COL_END_DATE_YEAR, item.endDateTime.year)
            put(EventsSchema.COL_END_TIME_HRS, item.endDateTime.hour)
            put(EventsSchema.COL_END_TIME_MINS, item.endDateTime.minute)
            put(EventsSchema.COL_LOCATION, item.location)
            put(EventsSchema.COL_RELATED_SUBJECT_ID, item.relatedSubjectId)
        }
        return values
    }

    override fun addItem(item: Event) {
        super.addItem(item)
        addAlarmForEvent(context, item)
    }

    override fun deleteItem(itemId: Int) {
        super.deleteItem(itemId)
        AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.EVENT, itemId)
    }

    companion object {
        @JvmStatic
        fun addAlarmForEvent(context: Context, event: Event) {
            if (!PrefUtils.getEventNotificationsEnabled(context)) {
                return // don't set alarm if event notifications disabled
            }

            val minsBefore = PrefUtils.getEventNotificationTime(context).toLong()
            val remindTime = event.startDateTime.minusMinutes(minsBefore)

            AlarmReceiver().setAlarm(context,
                    AlarmReceiver.Type.EVENT,
                    DateUtils.asCalendar(remindTime),
                    event.id)
        }
    }

}
