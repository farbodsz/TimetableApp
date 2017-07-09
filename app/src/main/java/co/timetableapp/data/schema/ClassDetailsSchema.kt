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
 * The schema for the 'class_details' table, containing constants for the column names and an
 * SQLite create statement.
 *
 * @see co.timetableapp.model.ClassDetail
 */
object ClassDetailsSchema : BaseColumns {

    const val TABLE_NAME = "class_details"
    const val _ID = BaseColumns._ID
    const val COL_CLASS_ID = "class_id"
    const val COL_ROOM = "room"
    const val COL_BUILDING = "building"
    const val COL_TEACHER = "teacher"

    /**
     * An SQLite statement which creates the 'class_details' table upon execution.
     *
     * @see co.timetableapp.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_CLASS_ID + INTEGER_TYPE + COMMA_SEP +
            COL_ROOM + TEXT_TYPE + COMMA_SEP +
            COL_BUILDING + TEXT_TYPE + COMMA_SEP +
            COL_TEACHER + TEXT_TYPE +
            " )"

}
