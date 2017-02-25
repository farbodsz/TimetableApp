package com.satsumasoftware.timetable.db

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.satsumasoftware.timetable.BuildConfig
import org.threeten.bp.LocalDateTime
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

object DatabaseUtils {

    private const val LOG_TAG = "DatabaseUtils"

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

        if (localDatabase.exists()) {
            Log.d(LOG_TAG, "Deleting local database before import")
            localDatabase.delete()
        }

        checkStoragePermissions(activity)

        copyFile(FileInputStream(newDatabase), FileOutputStream(localDatabase))

        // Access the imported database so that it will be cached and marked as created
        TimetableDbHelper.getInstance(activity).writableDatabase.close()

        Log.i(LOG_TAG, "Successfully imported database")
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
