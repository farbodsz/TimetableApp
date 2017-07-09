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

package co.timetableapp.data.handler

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import co.timetableapp.data.schema.AssignmentsSchema
import co.timetableapp.model.Assignment

class AssignmentHandler(context: Context) : TimetableItemHandler<Assignment>(context) {

    override val tableName = AssignmentsSchema.TABLE_NAME

    override val itemIdCol = AssignmentsSchema._ID

    override val timetableIdCol = AssignmentsSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Assignment.from(cursor)

    override fun createFromId(id: Int) = Assignment.create(context, id)

    override fun propertiesAsContentValues(item: Assignment): ContentValues {
        val values = ContentValues()
        with(values) {
            put(AssignmentsSchema._ID, item.id)
            put(AssignmentsSchema.COL_TIMETABLE_ID, item.timetableId)
            put(AssignmentsSchema.COL_CLASS_ID, item.classId)
            put(AssignmentsSchema.COL_TITLE, item.title)
            put(AssignmentsSchema.COL_DETAIL, item.detail)
            put(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH, item.dueDate.dayOfMonth)
            put(AssignmentsSchema.COL_DUE_DATE_MONTH, item.dueDate.monthValue)
            put(AssignmentsSchema.COL_DUE_DATE_YEAR, item.dueDate.year)
            put(AssignmentsSchema.COL_COMPLETION_PROGRESS, item.completionProgress)
        }
        return values
    }

}
