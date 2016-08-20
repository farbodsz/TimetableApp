package com.satsumasoftware.timetable

import android.content.Context
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.util.*

private const val ID_OVERDUE = 0
private const val ID_TODAY = 1
private const val ID_TOMORROW = 2
private const val ID_THIS_WEEK = 3
private const val ID_NEXT_WEEK = 4
private const val ID_THIS_MONTH = 5
private const val ID_LATER = 6

fun getDatePeriodId(dueDate: LocalDate): Int {
    val now = LocalDate.now()
    val woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()  // to get week of year
    return when {
        dueDate.isBefore(now) -> ID_OVERDUE
        dueDate.isEqual(now) -> ID_TODAY
        dueDate.isEqual(now.plusDays(1)) -> ID_TOMORROW
        dueDate.year == now.year && dueDate.get(woy) == now.get(woy) -> ID_THIS_WEEK
        dueDate.year == now.year && dueDate.get(woy) == now.plusWeeks(1).get(woy) -> ID_NEXT_WEEK
        dueDate.year == now.year && dueDate.monthValue == now.monthValue -> ID_THIS_MONTH
        else -> ID_LATER
    }
}

fun makeHeaderName(context: Context, timePeriodId: Int): String {
    val stringRes = when (timePeriodId) {
        ID_OVERDUE -> R.string.due_overdue
        ID_TODAY -> R.string.due_today
        ID_TOMORROW -> R.string.due_tomorrow
        ID_THIS_WEEK -> R.string.due_this_week
        ID_NEXT_WEEK -> R.string.due_next_week
        ID_THIS_MONTH -> R.string.due_this_month
        ID_LATER -> R.string.due_later
        else -> throw IllegalArgumentException("invalid time period id '$timePeriodId'")
    }
    return context.getString(stringRes)
}