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
 * The schema for the 'subjects' table, containing constants for the column names and an SQLite
 * create statement.
 *
 * @see co.timetableapp.model.Subject
 */
object SubjectsSchema : BaseColumns {

    const val TABLE_NAME = "subjects"
    const val _ID = BaseColumns._ID
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_NAME = "name"
    const val COL_ABBREVIATION = "abbreviation"
    const val COL_COLOR_ID = "color_id"

    /**
     * An SQLite statement which creates the 'subjects' table upon execution.
     *
     * @see co.timetableapp.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_NAME + TEXT_TYPE + COMMA_SEP +
            COL_ABBREVIATION + TEXT_TYPE + COMMA_SEP +
            COL_COLOR_ID + INTEGER_TYPE +
            " )"

}
