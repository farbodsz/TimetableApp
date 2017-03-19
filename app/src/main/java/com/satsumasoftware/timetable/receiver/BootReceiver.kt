package com.satsumasoftware.timetable.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.util.NotificationUtils

/**
 * Receives broadcasts - specifically, when the device has finished being booted.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val LOG_TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(LOG_TAG, "@onReceive() - ACTION_BOOT_COMPLETED - now refreshing alarms...")

            val application = context!!.applicationContext as TimetableApplication
            NotificationUtils.refreshAlarms(context, application)
        }
    }

}
