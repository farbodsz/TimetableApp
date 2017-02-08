package com.satsumasoftware.timetable.db.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.TimetableApplication
import com.satsumasoftware.timetable.db.DataHandlers
import com.satsumasoftware.timetable.db.DataUtils
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.db.schema.SubjectsSchema
import com.satsumasoftware.timetable.db.schema.TermsSchema
import com.satsumasoftware.timetable.framework.Timetable

object TimetableUtils {

    private const val LOG_TAG = "TimetableUtils"

    @JvmStatic
    fun replaceTimetable(activity: Activity, oldTimetableId: Int, newTimetable: Timetable) {
        Log.i(LOG_TAG, "Replacing Timetable...")

        DataUtils.deleteItem(DataHandlers.TIMETABLES, activity, oldTimetableId)
        DataUtils.addItem(DataHandlers.TIMETABLES, activity, newTimetable)

        // Refresh alarms in case start/end dates have changed
        (activity.application as TimetableApplication).refreshAlarms(activity)
    }

    @JvmStatic
    fun completelyDeleteTimetable(context: Context, timetableId: Int) {
        Log.i(LOG_TAG, "Deleting everything related to Timetable of id $timetableId")

        DataUtils.deleteItem(DataHandlers.TIMETABLES, context, timetableId)

        // Note that we only need to delete subjects, terms and their references since classes,
        // assignments, exams, and everything else are linked to subjects.

        val subjectsQuery = Query.Builder()
                .addFilter(Filters.equal(SubjectsSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .build()

        for (subject in DataUtils.getAllItems(DataHandlers.SUBJECTS, context, subjectsQuery)) {
            SubjectUtils.completelyDeleteSubject(context, subject)
        }

        val termsQuery = Query.Builder()
                .addFilter(Filters.equal(TermsSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .build()

        DataUtils.getAllItems(DataHandlers.TERMS, context, termsQuery).forEach {
            DataUtils.deleteItem(DataHandlers.TERMS, context, it.id)
        }
    }

}
