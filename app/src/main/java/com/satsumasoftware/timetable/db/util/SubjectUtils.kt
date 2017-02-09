package com.satsumasoftware.timetable.db.util

import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.ClassUtils
import com.satsumasoftware.timetable.db.ExamUtils
import com.satsumasoftware.timetable.db.SubjectUtils
import com.satsumasoftware.timetable.db.query.Filters
import com.satsumasoftware.timetable.db.query.Query
import com.satsumasoftware.timetable.db.schema.ClassesSchema
import com.satsumasoftware.timetable.db.schema.ExamsSchema
import com.satsumasoftware.timetable.framework.Subject

object SubjectUtils {

    private const val LOG_TAG = "SubjectUtils"

    @JvmStatic
    fun completelyDeleteSubject(context: Context, subject: Subject) {
        Log.i(LOG_TAG, "Deleting everything related to Subject with id ${subject.id}")

        SubjectUtils().deleteItem(context, subject.id)

        val classesQuery = Query.Builder()
                .addFilter(Filters.equal(ClassesSchema.COL_SUBJECT_ID, subject.id.toString()))
                .build()

        for (cls in ClassUtils().getAllItems(context, classesQuery)) {
            ClassUtils.completelyDeleteClass(context, cls)
        }

        val examsQuery = Query.Builder()
                .addFilter(Filters.equal(ExamsSchema.COL_SUBJECT_ID, subject.id.toString()))
                .build()

        val examUtils = ExamUtils()
        for (exam in examUtils.getAllItems(context, examsQuery)) {
            examUtils.deleteItem(context, exam.id)
        }
    }

}
