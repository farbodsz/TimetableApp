/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.util

import android.app.Application
import co.timetableapp.TimetableApplication
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

object DateUtils {

    /**
     * [DateTimeFormatter] for showing 24-hour times - e.g. 17:45
     */
    @JvmField val FORMATTER_TIME = DateTimeFormatter.ofPattern("HH:mm")!!

    /**
     * [DateTimeFormatter] for showing short dates - e.g. 7 May
     */
    @JvmField val FORMATTER_SHORT_DATE = DateTimeFormatter.ofPattern("d MMM")!!

    /**
     * [DateTimeFormatter] for showing dates in full - e.g. "28 September 2014".
     */
    @JvmField val FORMATTER_FULL_DATE = DateTimeFormatter.ofPattern("d MMMM uuuu")!!

    /**
     * [DateTimeFormatter] for showing months with years in full - e.g. "March 2012".
     */
    @JvmField val FORMATTER_MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM uuuu")!!

    /**
     * [DateTimeFormatter] for showing month abbreviations with years - e.g. "Mar 2012".
     */
    @JvmField val FORMATTER_SHORT_MONTH_YEAR = DateTimeFormatter.ofPattern("MMM uuuu")!!

    @JvmOverloads
    @JvmStatic
    fun findWeekNumber(application: Application, localDate: LocalDate = LocalDate.now()): Int {
        val timetable = (application as TimetableApplication).currentTimetable!!

        val days = Period.between(timetable.startDate, localDate).days
        val nthWeek = days / 7

        val weekNo = (nthWeek % timetable.weekRotations) + 1
        return weekNo
    }

    @JvmStatic
    fun asCalendar(localDateTime: LocalDateTime) = GregorianCalendar(
            localDateTime.year,
            localDateTime.monthValue - 1,
            localDateTime.dayOfMonth,
            localDateTime.hour,
            localDateTime.minute)

}
