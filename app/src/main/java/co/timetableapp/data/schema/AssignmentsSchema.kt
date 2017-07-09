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
 * The schema for the 'assignments' table, containing constants for the column names and the SQLite
 * create statement.
 *
 * @see co.timetableapp.model.Assignment
 */
object AssignmentsSchema : BaseColumns {

    const val TABLE_NAME = "assignments"
    const val _ID = BaseColumns._ID
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_CLASS_ID = "class_id"
    const val COL_TITLE = "title"
    const val COL_DETAIL = "detail"
    const val COL_DUE_DATE_DAY_OF_MONTH = "due_date_day_of_month"
    const val COL_DUE_DATE_MONTH = "due_date_month"
    const val COL_DUE_DATE_YEAR = "due_date_year"
    const val COL_COMPLETION_PROGRESS = "completion_progress"

    /**
     * An SQLite statement which creates the 'assignments' table upon execution.
     *
     * @see co.timetableapp.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_CLASS_ID + INTEGER_TYPE + COMMA_SEP +
            COL_TITLE + TEXT_TYPE + COMMA_SEP +
            COL_DETAIL + TEXT_TYPE + COMMA_SEP +
            COL_DUE_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_DUE_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_DUE_DATE_YEAR + INTEGER_TYPE + COMMA_SEP +
            COL_COMPLETION_PROGRESS + INTEGER_TYPE +
            " )"

}
