package com.satsumasoftware.timetable.db.util

import android.content.Context
import android.util.Log
import com.satsumasoftware.timetable.db.DataHandlers
import com.satsumasoftware.timetable.db.DataUtils
import com.satsumasoftware.timetable.framework.Subject

object SubjectUtils {

    private const val LOG_TAG = "SubjectUtils"

    @JvmStatic
    fun completelyDeleteSubject(context: Context, subject: Subject) {
        Log.i(LOG_TAG, "Deleting everything related to Subject with id ${subject.id}")

        DataUtils.deleteItem(DataHandlers.SUBJECTS, context, subject.id)

        for (cls in ClassUtils.getClassesForSubject(context, subject.id)) {
            ClassUtils.completelyDeleteClass(context, cls)
        }

        for (exam in ExamUtils.getExamsForSubject(context, subject.id)) {
            ExamUtils.deleteExam(context, exam.id)
        }
    }

}
