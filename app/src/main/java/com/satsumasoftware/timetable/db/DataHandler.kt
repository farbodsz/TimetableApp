package com.satsumasoftware.timetable.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.framework.BaseItem
import java.util.*

abstract class DataHandler<T : BaseItem>(val context: Context) {

    companion object {
        private const val LOG_TAG = "DataHandler"
    }

    abstract val tableName: String

    abstract val itemIdCol: String

    abstract fun createFromCursor(cursor: Cursor): T

    abstract fun propertiesAsContentValues(item: T): ContentValues

    @JvmOverloads
    fun getAllItems(query: Query? = null): ArrayList<T> {
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

    fun getHighestItemId(): Int {
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

    open fun addItem(item: T) {
        val values = propertiesAsContentValues(item)

        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.insert(tableName, null, values)

        Log.i(LOG_TAG, "Added item with id ${item.id} to $tableName")
    }

    open fun deleteItem(itemId: Int) {
        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.delete(tableName,
                "$itemIdCol=?",
                arrayOf(itemId.toString()))
        Log.i(LOG_TAG, "Deleted item with id $itemId from $tableName")
    }

    open fun replaceItem(oldItemId: Int, newItem: T) {
        Log.i(LOG_TAG, "Replacing item...")
        deleteItem(oldItemId)
        addItem(newItem)
    }

    open fun deleteItemWithReferences(itemId: Int) {
        deleteItem(itemId)
    }

}
