package com.satsumasoftware.timetable.db

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
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
            "Timetables-Exported-" + LocalDateTime.now().toString()


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

        Log.i(LOG_TAG, "Successfully exported database")
        return true
    }

    private fun copyFile(fromFile: FileInputStream, toFile: FileOutputStream) {
        var fromChannel: FileChannel? = null
        var toChannel: FileChannel? = null
        try {
            fromChannel = fromFile.getChannel()
            toChannel = toFile.getChannel()
            fromChannel!!.transferTo(0, fromChannel!!.size(), toChannel)
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel!!.close()
                }
            } finally {
                if (toChannel != null) {
                    toChannel!!.close()
                }
            }
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
