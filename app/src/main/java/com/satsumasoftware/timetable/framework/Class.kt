package com.satsumasoftware.timetable.framework

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

class Class(val id: Int, val subjectId: Int, val day: DayOfWeek, val startTime: LocalTime,
            val endTime: LocalTime, val room: String, val teacher: String)
