package com.satsumasoftware.timetable

import android.content.Context
import android.preference.PreferenceManager
import com.satsumasoftware.timetable.db.util.TimetableUtils
import com.satsumasoftware.timetable.framework.Timetable

class PrefUtils {

    companion object {

        const val PREF_CURRENT_TIMETABLE = "pref_current_timetable"

        @JvmStatic fun getCurrentTimetable(context: Context): Timetable? {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val timetableId = sp.getInt(PREF_CURRENT_TIMETABLE, -1)
            return if (timetableId == -1) {
                null
            } else {
                TimetableUtils.getTimetableWithId(context, timetableId)
            }
        }

        @JvmStatic fun setCurrentTimetable(context: Context, timetable: Timetable) {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            sp.edit().putInt(PREF_CURRENT_TIMETABLE, timetable.id).apply()
        }

    }
}
