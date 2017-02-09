package com.satsumasoftware.timetable.db.util

import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.SubjectUtils
import com.satsumasoftware.timetable.db.TermUtils
import com.satsumasoftware.timetable.db.TimetableUtils
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.db.schema.SubjectsSchema
import com.satsumasoftware.timetable.db.schema.TermsSchema

object TimetableUtils {

    private const val LOG_TAG = "TimetableUtils"

    @JvmStatic
    fun completelyDeleteTimetable(context: Context, timetableId: Int) {
        Log.i(LOG_TAG, "Deleting everything related to Timetable of id $timetableId")

        TimetableUtils().deleteItem(context, timetableId)

        // Note that we only need to delete subjects, terms and their references since classes,
        // assignments, exams, and everything else are linked to subjects.

        val subjectsQuery = Query.Builder()
                .addFilter(Filters.equal(SubjectsSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .build()

        for (subject in SubjectUtils().getAllItems(context, subjectsQuery)) {
            SubjectUtils.completelyDeleteSubject(context, subject)
        }

        val termsQuery = Query.Builder()
                .addFilter(Filters.equal(TermsSchema.COL_TIMETABLE_ID, timetableId.toString()))
                .build()

        val termUtils = TermUtils()
        termUtils.getAllItems(context, termsQuery).forEach {
            termUtils.deleteItem(context, it.id)
        }
    }

}
