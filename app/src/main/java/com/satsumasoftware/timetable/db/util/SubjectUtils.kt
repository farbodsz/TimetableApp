package com.satsumasoftware.timetable.db.util

import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.DataHandlers
import com.satsumasoftware.timetable.db.DataUtils
import com.satsumasoftware.timetable.db.schema.ClassesSchema
import com.satsumasoftware.timetable.db.schema.ExamsSchema
import com.satsumasoftware.timetable.framework.Subject
import com.satsumasoftware.timetable.query.Filters
import com.satsumasoftware.timetable.query.Query

object SubjectUtils {

    private const val LOG_TAG = "SubjectUtils"

    @JvmStatic
    fun completelyDeleteSubject(context: Context, subject: Subject) {
        Log.i(LOG_TAG, "Deleting everything related to Subject with id ${subject.id}")

        DataUtils.deleteItem(DataHandlers.SUBJECTS, context, subject.id)

        val classesQuery = Query.Builder()
                .addFilter(Filters.equal(ClassesSchema.COL_SUBJECT_ID, subject.id.toString()))
                .build()

        for (cls in DataUtils.getAllItems(DataHandlers.CLASSES, context, classesQuery)) {
            ClassUtils.completelyDeleteClass(context, cls)
        }

        val examsQuery = Query.Builder()
                .addFilter(Filters.equal(ExamsSchema.COL_SUBJECT_ID, subject.id.toString()))
                .build()

        for (exam in DataUtils.getAllItems(DataHandlers.EXAMS, context, examsQuery)) {
            ExamUtils.deleteExam(context, exam.id)
        }
    }

}
