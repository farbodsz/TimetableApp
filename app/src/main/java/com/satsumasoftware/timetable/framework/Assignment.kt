package com.satsumasoftware.timetable.framework

import org.threeten.bp.LocalDate

class Assignment(val id: Int, val classId: Int, val title: String, val detail: String,
                 val dueDate: LocalDate, val completionProgress: Int)
