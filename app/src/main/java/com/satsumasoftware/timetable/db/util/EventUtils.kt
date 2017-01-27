package com.satsumasoftware.timetable.db.util

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.EventsSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.framework.Event
import com.satsumasoftware.timetable.receiver.AlarmReceiver
import com.satsumasoftware.timetable.util.DateUtils
import java.util.*

class EventUtils {

    companion object {

        private const val LOG_TAG = "EventUtils"

        @JvmStatic
        fun getEvents(context: Context, application: Application): ArrayList<Event> {
            val events = ArrayList<Event>()

            val timetable = (application as TimetableApplication).currentTimetable!!

            val dbHelper = TimetableDbHelper.getInstance(context)
            val cursor = dbHelper.readableDatabase.query(
                    EventsSchema.TABLE_NAME,
                    null,
                    "${EventsSchema.COL_TIMETABLE_ID}=?",
                    arrayOf(timetable.id.toString()),
                    null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                events.add(Event.from(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            return events
        }

        @JvmStatic
        fun getAllEvents(context: Context): ArrayList<Event> {
            val events = ArrayList<Event>()
            val dbHelper = TimetableDbHelper.getInstance(context)
            val cursor = dbHelper.readableDatabase.query(
                    EventsSchema.TABLE_NAME, null, null, null, null, null, null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                events.add(Event.from(cursor))
                cursor.moveToNext()
            }
            cursor.close()
            return events
        }

        @JvmStatic
        fun addEvent(context: Context, event: Event) {
            val values = ContentValues()
            with(values) {
                put(EventsSchema._ID, event.id)
                put(EventsSchema.COL_TIMETABLE_ID, event.timetableId)
                put(EventsSchema.COL_TITLE, event.title)
                put(EventsSchema.COL_DETAIL, event.detail)
                put(EventsSchema.COL_START_DATE_DAY_OF_MONTH, event.startTime.dayOfMonth)
                put(EventsSchema.COL_START_DATE_MONTH, event.startTime.monthValue)
                put(EventsSchema.COL_START_DATE_YEAR, event.startTime.year)
                put(EventsSchema.COL_START_TIME_HRS, event.startTime.hour)
                put(EventsSchema.COL_START_TIME_MINS, event.startTime.minute)
                put(EventsSchema.COL_END_DATE_DAY_OF_MONTH, event.endTime.dayOfMonth)
                put(EventsSchema.COL_END_DATE_MONTH, event.endTime.monthValue)
                put(EventsSchema.COL_END_DATE_YEAR, event.endTime.year)
                put(EventsSchema.COL_END_TIME_HRS, event.endTime.hour)
                put(EventsSchema.COL_END_TIME_MINS, event.endTime.minute)
            }

            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.insert(EventsSchema.TABLE_NAME, null, values)

            addAlarmForEvent(context, event)

            Log.i(LOG_TAG, "Added Event with id ${event.id}")
        }

        @JvmStatic
        fun addAlarmForEvent(context: Context, event: Event) {
            val remindTime = event.startTime.minusMinutes(30) // TODO make this customizable
            AlarmReceiver().setAlarm(context,
                    AlarmReceiver.Type.EVENT,
                    DateUtils.asCalendar(remindTime),
                    event.id)
        }

        @JvmStatic
        fun deleteEvent(context: Context, eventId: Int) {
            val db = TimetableDbHelper.getInstance(context).writableDatabase
            db.delete(EventsSchema.TABLE_NAME,
                    "${EventsSchema._ID}=?",
                    arrayOf(eventId.toString()))

            AlarmReceiver().cancelAlarm(context, AlarmReceiver.Type.EVENT, eventId)

            Log.i(LOG_TAG, "Deleted Event with id $eventId")
        }

        @JvmStatic
        fun replaceEvent(context: Context, oldEventId: Int, newEvent: Event) {
            Log.i(LOG_TAG, "Replacing Event...")
            deleteEvent(context, oldEventId)
            addEvent(context, newEvent)
        }

        @JvmStatic
        fun getHighestEventId(context: Context): Int {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    EventsSchema.TABLE_NAME,
                    arrayOf(EventsSchema._ID),
                    null,
                    null,
                    null,
                    null,
                    "${EventsSchema._ID} DESC")
            if (cursor.count == 0) {
                return 0
            }
            cursor.moveToFirst()
            val highestId = cursor.getInt(cursor.getColumnIndex(EventsSchema._ID))
            cursor.close()
            return highestId
        }

    }
}
