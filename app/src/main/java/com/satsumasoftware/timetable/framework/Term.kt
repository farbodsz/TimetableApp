package com.satsumasoftware.timetable.framework

import org.threeten.bp.LocalDate

class Term(val id: Int, val timetableId: Int, val name: String, val startDate: LocalDate,
           val endDate: LocalDate)
