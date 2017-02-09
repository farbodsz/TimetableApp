package com.satsumasoftware.timetable.db.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.framework.BaseItem
import java.util.*

/**
 * The base data handler class.
 *
 * It provides implementations of database-related functions using a generic type (specifically, a
 * subclass of [BaseItem]). Since the table to read from, column names, and the method of adding
 * data from the data model class to the database table vary between the different types, subclasses
 * of this base handler must provide definitions to some fields and methods.
 *
 * Furthermore, some of the functions can be overridden to incorporate additional functionality when
 * performing a common task - for example, some subclasses may want to add a notification alarm
 * after the item has been added to the database.
 */
abstract class DataHandler<T : BaseItem>(val context: Context) {

    companion object {
        private const val LOG_TAG = "DataHandler"
    }

    /**
     * The name of the database table for reading/writing data.
     * It is used in almost every function from this base class.
     */
    abstract val tableName: String

    /**
     * The column name of the column storing the integer identifiers in the table.
     */
    abstract val itemIdCol: String

    /**
     * Constructs the data model type using column values from the cursor provided.
     *
     * @see addItem
     */
    abstract fun createFromCursor(cursor: Cursor): T

    /**
     * Puts properties of the data model type into [ContentValues] and returns this.
     *
     * @see addItem
     */
    abstract fun propertiesAsContentValues(item: T): ContentValues

    /**
     * @param query the condition for selecting data items from the table. If this is null, all
     *          data items will be selected.
     * @return a list of the selected data items of type [T]
     */
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

    /**
     * @return the id of the most recently added item (will have the highest id). This is typically
     *          used as a way of determining the id of a new item to be added.
     */
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

    /**
     * Adds an item's data to the database table.
     *
     * @param item the data model class reference for the item to be added
     * @see deleteItem
     * @see replaceItem
     */
    open fun addItem(item: T) {
        val values = propertiesAsContentValues(item)

        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.insert(tableName, null, values)

        Log.i(LOG_TAG, "Added item with id ${item.id} to $tableName")
    }

    /**
     * Removes an item of type [T], with the specified integer identifier, to the database table.
     *
     * @see addItem
     * @see replaceItem
     * @see deleteItemWithReferences
     */
    open fun deleteItem(itemId: Int) {
        val db = TimetableDbHelper.getInstance(context).writableDatabase
        db.delete(tableName,
                "$itemIdCol=?",
                arrayOf(itemId.toString()))
        Log.i(LOG_TAG, "Deleted item with id $itemId from $tableName")
    }

    /**
     * Replaces an item of type [T] using its old integer identifier and a reference to the new data
     * model class.
     * By default, it merely deletes the old item using the id, and adds it again using values from
     * the new data model class.
     *
     * @see addItem
     * @see deleteItem
     */
    open fun replaceItem(oldItemId: Int, newItem: T) {
        Log.i(LOG_TAG, "Replacing item...")
        deleteItem(oldItemId)
        addItem(newItem)
    }

    /**
     * Deletes the item from the table and deletes all references to it (which would otherwise be
     * redundant data in the table).
     * By default, this function only deletes the item (assumes there are no references). Subclasses
     * should override to specify their own additional behaviour.
     *
     * @see deleteItem
     */
    open fun deleteItemWithReferences(itemId: Int) {
        deleteItem(itemId)
    }

}
