package com.satsumasoftware.timetable.framework

import org.threeten.bp.LocalTime
import java.util.*

class ClassTimeGroup(val startTime: LocalTime, val endTime: LocalTime) {

    val classTimes = ArrayList<ClassTime>()

    fun addClassTime(classTime: ClassTime) {
        if (!canAdd(classTime)) {
            throw IllegalArgumentException("invalid class time - the start and end times must" +
                    "match the ones specified from this object's constructor")
        }
        classTimes.add(classTime)
    }

    fun canAdd(classTime: ClassTime) =
            classTime.startTime.equals(startTime) && classTime.endTime.equals(endTime)

}
