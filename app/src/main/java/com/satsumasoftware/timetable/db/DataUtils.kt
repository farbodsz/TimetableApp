package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.framework.BaseItem
import java.util.*

interface DataUtils<T : BaseItem> {

    companion object {
        private const val LOG_TAG = "DataUtils"
    }

    val tableName: String

    val itemIdCol: String

    fun createFromCursor(cursor: Cursor): T

    fun propertiesAsContentValues(item: T): ContentValues

    @JvmOverloads
    fun getAllItems(context: Context, query: Query? = null): ArrayList<T> {
        val items = ArrayList<T>()

        val dbHelper = TimetableDbHelper.getInstance(context)
        val cursor = dbHelper.readableDatabase.query(
                tableName,
                null,
                query?.filter?.sqlStatement,
                null, null, null, null)
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            items.add(createFromCursor(cursor))
            cursor.moveToNext()
        }
        cursor.close()

        return items
    }

    fun getHighestItemId(context: Context): Int {
        val db = TimetableDbHelper.getInstance(context).readableDatabase
        val cursor = db.query(
                tableName,
                arrayOf(itemIdCol),
                null,
                null,
                null,
                null,
                "$itemIdCol DESC")
        if (cursor.count == 0) {
            return 0
        }
        cursor.moveToFirst()
        val highestId = cursor.getInt(cursor.getColumnIndex(itemIdCol))
        cursor.close()
        return highestId
    }

    fun addItem(context: Context, item: T) {
        val values = propertiesAsContentValues(item)

        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.insert(tableName, null, values)

        Log.i(LOG_TAG, "Added item with id ${item.id} to $tableName")
    }

    fun deleteItem(context: Context, itemId: Int) {
        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.delete(tableName,
                "$itemIdCol=?",
                arrayOf(itemId.toString()))
        Log.i(LOG_TAG, "Deleted item with id $itemId from $tableName")
    }

    fun replaceItem(context: Context, oldItemId: Int, newItem: T) {
        Log.i(LOG_TAG, "Replacing item...")
        deleteItem(context, oldItemId)
        addItem(context, newItem)
    }

}
