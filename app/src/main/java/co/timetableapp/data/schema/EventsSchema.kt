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

package co.timetableapp.data.schema

import android.provider.BaseColumns

/**
 * The schema for the 'events' table, containing constants for the column names and an SQLite create
 * statement.
 *
 * @see co.timetableapp.model.Event
 */
object EventsSchema : BaseColumns {

    const val TABLE_NAME = "events"
    const val _ID = BaseColumns._ID
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_TITLE = "title"
    const val COL_DETAIL = "detail"
    const val COL_START_DATE_DAY_OF_MONTH = "start_date_day_of_month"
    const val COL_START_DATE_MONTH = "start_date_month"
    const val COL_START_DATE_YEAR = "start_date_year"
    const val COL_START_TIME_HRS = "start_time_hrs"
    const val COL_START_TIME_MINS = "start_time_mins"
    const val COL_END_DATE_DAY_OF_MONTH = "end_date_day_of_month"
    const val COL_END_DATE_MONTH = "end_date_month"
    const val COL_END_DATE_YEAR = "end_date_year"
    const val COL_END_TIME_HRS = "end_time_hrs"
    const val COL_END_TIME_MINS = "end_time_mins"
    const val COL_LOCATION = "location"
    const val COL_RELATED_SUBJECT_ID = "related_subject_id"

    /**
     * An SQLite statement which creates the 'events' table upon execution.
     *
     * @see co.timetableapp.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_TITLE + TEXT_TYPE + COMMA_SEP +
            COL_DETAIL + TEXT_TYPE + COMMA_SEP +
            COL_START_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_START_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_START_DATE_YEAR + INTEGER_TYPE + COMMA_SEP +
            COL_START_TIME_HRS + INTEGER_TYPE + COMMA_SEP +
            COL_START_TIME_MINS + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_YEAR + INTEGER_TYPE + COMMA_SEP +
            COL_END_TIME_HRS + INTEGER_TYPE + COMMA_SEP +
            COL_END_TIME_MINS + INTEGER_TYPE + COMMA_SEP +
            COL_LOCATION + TEXT_TYPE + COMMA_SEP +
            COL_RELATED_SUBJECT_ID + INTEGER_TYPE +
            " )"

}
