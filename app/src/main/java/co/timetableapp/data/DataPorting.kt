/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.data

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.util.Log
import co.timetableapp.BuildConfig
import co.timetableapp.data.schema.TimetablesSchema
import org.threeten.bp.LocalDateTime
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

object DataPorting {

    private const val LOG_TAG = "DataPorting"

    private const val DB_PATH = "data/data/" + BuildConfig.APPLICATION_ID + "/databases/" +
            TimetableDbHelper.DATABASE_NAME

    @JvmStatic private val DEFAULT_EXPORT_DIRECTORY =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    @JvmStatic private val DEFAULT_EXPORT_FILENAME =
            "Timetables-Exported-" + LocalDateTime.now().toString() + ".db"


    @JvmStatic
    fun exportDatabase(activity: Activity): Boolean {
        val localDatabase = File(DB_PATH)
        val exportedFile = File(DEFAULT_EXPORT_DIRECTORY, DEFAULT_EXPORT_FILENAME)

        if (!localDatabase.exists()) {
            Log.wtf(LOG_TAG, "Local database doesn't exist?!")
            return false
        }

        checkStoragePermissions(activity)

        copyFile(FileInputStream(localDatabase), FileOutputStream(exportedFile))

        Log.i(LOG_TAG, "Successfully exported database to: " + exportedFile.path)
        return true
    }

    @JvmStatic
    fun importDatabase(activity: Activity, importData: Uri): Boolean {
        val newDatabase = File(convertUriToFilePath(importData))
        val localDatabase = File(DB_PATH)

        if (!newDatabase.exists()) {
            Log.e(LOG_TAG, "Database path not found: ${newDatabase.path}")
            return false
        }

        if (!localDatabase.exists()) {
            // Local db doesn't exist - create it to avoid FileNotFoundException later
            Log.d(LOG_TAG, "Local database doesn't exist - creating a blank dummy")
            TimetableDbHelper.getInstance(activity).writableDatabase.close()
        }

        if (localDatabase.exists()) {
            Log.d(LOG_TAG, "Deleting local database before import")
            TimetableDbHelper.getInstance(activity).close()
            localDatabase.delete()
        }

        checkStoragePermissions(activity)

        copyFile(FileInputStream(newDatabase), FileOutputStream(localDatabase))

        // Access the imported database so that it will be cached and marked as created
        TimetableDbHelper.getInstance(activity).writableDatabase.close()

        Log.i(LOG_TAG, "Successfully imported database")
        return true
    }

    /**
     * Checks whether a file is a valid database that can be imported into the app (with
     * [importDatabase]).
     * This is decided by the file extension (should be `.db`) and then if the database contains the
     * timetables table.
     *
     * @param importData the uri for the file to be imported
     * @return if the file is a valid importable database
     *
     * @see TimetablesSchema
     */
    @JvmStatic
    fun isDatabaseValid(importData: Uri): Boolean {
        // Check file extension using path name
        val path = importData.path
        var extension = ""
        if (path.contains(".")) {
            extension = path.substring(path.lastIndexOf("."))
        }
        if (extension != ".db") {
            Log.v(LOG_TAG, "Importing file has incorrect extension: '$extension'")
            return false
        }

        val importingDb = SQLiteDatabase.openDatabase(convertUriToFilePath(importData), null, 0)

        // Check if 'timetables' table exists (it should always exist for timetable app data)
        val tableCheckCursor = importingDb.rawQuery("SELECT DISTINCT tbl_name " +
                "FROM sqlite_master WHERE tbl_name = '${TimetablesSchema.TABLE_NAME}'", null)
        val cursorCount = tableCheckCursor.count
        tableCheckCursor.close()

        if (cursorCount == 0) {
            Log.v(LOG_TAG, "Importing file has missing timetables table: " +
                    "'${TimetablesSchema.TABLE_NAME}'")
            return false
        }

        // The file looks okay for importing
        return true
    }

    private fun convertUriToFilePath(importData: Uri): String {
        val path = importData.path
        // TODO not sure if this always works
        return path.replace("/document/primary:", "/storage/emulated/0/")
    }

    private fun copyFile(fromFile: FileInputStream, toFile: FileOutputStream) {
        var fromChannel: FileChannel? = null
        var toChannel: FileChannel? = null
        try {
            fromChannel = fromFile.channel
            toChannel = toFile.channel
            fromChannel!!.transferTo(0, fromChannel.size(), toChannel)
        } finally {
            if (fromChannel != null) fromChannel.close()
            if (toChannel != null) toChannel.close()
        }
    }

    @JvmStatic
    fun checkStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permStatus = ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permStatus != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission - this should have been requested in the activity
            throw IllegalStateException("Permission for writing to storage is denied. " +
                    "This should be requested (and granted) in the calling activity.")
        }
    }

}
