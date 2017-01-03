package com.satsumasoftware.timetable.util

import android.content.Context
import android.preference.PreferenceManager
import com.google.android.gms.drive.DriveId

class DriveDbUtils private constructor() {

    companion object {

        private const val DATABASE_DRIVE_ID = "timetable_database_drive_id"

        @JvmStatic
        fun getDatabaseDriveId(context: Context): DriveId? {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val driveIdString = sp.getString(DATABASE_DRIVE_ID, null)
            return if (driveIdString == null) {
                null
            } else {
                DriveId.decodeFromString(driveIdString)
            }
        }

        @JvmStatic
        fun storeDatabaseDriveId(context: Context, driveId: DriveId) {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val driveIdString = driveId.encodeToString()
            sp.edit().putString(DATABASE_DRIVE_ID, driveIdString).apply()
        }

    }
}
