package com.satsumasoftware.timetable.db

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.framework.BaseItem
import java.util.*

object DataUtils {

    private const val LOG_TAG = "DataUtils"

    @JvmStatic
    fun <T : BaseItem> getItems(dataHandler: DataHandler<T>, activity: Activity) =
            getItems(dataHandler, activity, activity.application)

    @JvmStatic
    fun <T : BaseItem> getItems(dataHandler: DataHandler<T>, context: Context,
                                application: Application): ArrayList<T> {
        val items = ArrayList<T>()

        val timetable = (application as TimetableApplication).currentTimetable!!

        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                dataHandler.tableName,
                null,
                "${dataHandler.timetableIdCol}=?",
                arrayOf(timetable.id.toString()),
                null, null, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            items.add(dataHandler.createFromCursor(cursor))
            cursor.moveToNext()
        }
        cursor.close()

        return items
    }

    @JvmStatic
    fun <T : BaseItem> getHighestItemId(dataHandler: DataHandler<T>, context: Context): Int {
        val db = TimetableDbHelper.getInstance(context).readableDatabase
        val cursor = db.query(
                dataHandler.tableName,
                arrayOf(dataHandler.itemIdCol),
                null,
                null,
                null,
                null,
                "${dataHandler.itemIdCol} DESC")
        if (cursor.count == 0) {
            return 0
        }
        cursor.moveToFirst()
        val highestId = cursor.getInt(cursor.getColumnIndex(dataHandler.itemIdCol))
        cursor.close()
        return highestId
    }

    @JvmStatic
    private fun <T : BaseItem> deleteItem(dataHandler: DataHandler<T>, context: Context, itemId: Int) {
        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.delete(dataHandler.tableName,
                "${dataHandler.itemIdCol}=?",
                arrayOf(itemId.toString()))
        Log.i(LOG_TAG, "Deleted item with id $itemId from ${dataHandler.tableName}")
    }

}
