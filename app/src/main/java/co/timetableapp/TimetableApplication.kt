package co.timetableapp

import android.app.Application
import android.content.Context
import android.util.Log
import co.timetableapp.model.Timetable
import co.timetableapp.util.NotificationUtils
import co.timetableapp.util.PrefUtils
import com.jakewharton.threetenabp.AndroidThreeTen

class TimetableApplication : Application() {

    private val LOG_TAG = "TimetableApplication"

    var currentTimetable: Timetable? = null
        private set(value) {
            field = value
            value?.let {
                PrefUtils.setCurrentTimetable(this, field!!)
                Log.i(LOG_TAG, "Switched current timetable to that with id ${field!!.id}")
            }
        }

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)

        currentTimetable = PrefUtils.getCurrentTimetable(this)
    }

    fun setCurrentTimetable(context: Context, timetable: Timetable) {
        currentTimetable = timetable
        NotificationUtils.refreshAlarms(context, this)
    }

}
