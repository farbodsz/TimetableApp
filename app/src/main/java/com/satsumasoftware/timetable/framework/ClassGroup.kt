package com.satsumasoftware.timetable.framework

import java.util.*

class ClassGroup(val subjectId: Int) {

    val classes = ArrayList<Class>()

    fun addClass(cls: Class) {
        if (cls.subjectId != subjectId) {
            throw IllegalArgumentException("the subject id of the Class, (${cls.subjectId}), " +
                    "must match that of this ClassGroup ($subjectId)")
        }

        classes.add(cls)
    }

}
