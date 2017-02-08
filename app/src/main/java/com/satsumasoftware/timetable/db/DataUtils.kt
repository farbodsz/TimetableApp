package com.satsumasoftware.timetable.db

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.framework.BaseItem
import com.satsumasoftware.timetable.framework.TimetableItem
import com.satsumasoftware.timetable.query.Filters
import com.satsumasoftware.timetable.query.Query
import java.util.*

object DataUtils {

    private const val LOG_TAG = "DataUtils"

    @JvmStatic
    fun <T : TimetableItem> getItems(dataHandler: TimetableItemDataHandler<T>, activity: Activity) =
            getItems(dataHandler, activity, activity.application)

    @JvmStatic
    fun <T : TimetableItem> getItems(dataHandler: TimetableItemDataHandler<T>, context: Context,
                                     application: Application): ArrayList<T> {
        val timetable = (application as TimetableApplication).currentTimetable!!

        val query = Query.Builder()
                .addFilter(Filters.equal(dataHandler.timetableIdCol, timetable.id.toString()))
                .build()

        return getAllItems(dataHandler, context, query)
    }

    @JvmStatic
    @JvmOverloads
    fun <T : BaseItem> getAllItems(dataHandler: DataHandler<T>, context: Context,
                                   query: Query? = null): ArrayList<T> {
        val items = ArrayList<T>()

        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                dataHandler.tableName,
                null,
                query?.filter?.sqlStatement,
                null, null, null, null)
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
    fun <T : BaseItem> addItem(dataHandler: DataHandler<T>, context: Context, item: T) {
        val values = dataHandler.propertiesAsContentValues(item)

        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.insert(dataHandler.tableName, null, values)

        Log.i(LOG_TAG, "Added item with id ${item.id} to ${dataHandler.tableName}")
    }

    @JvmStatic
    fun <T : BaseItem> deleteItem(dataHandler: DataHandler<T>, context: Context,
                                          itemId: Int) {
        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.delete(dataHandler.tableName,
                "${dataHandler.itemIdCol}=?",
                arrayOf(itemId.toString()))
        Log.i(LOG_TAG, "Deleted item with id $itemId from ${dataHandler.tableName}")
    }

    @JvmStatic
    fun <T : BaseItem> replaceItem(dataHandler: DataHandler<T>, context: Context, oldItemId: Int,
                                   newItem: T) {
        Log.i(LOG_TAG, "Replacing item...")
        deleteItem(dataHandler, context, oldItemId)
        addItem(dataHandler, context, newItem)
    }

}
