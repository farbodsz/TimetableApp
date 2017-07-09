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
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.data.schema.ClassesSchema
import co.timetableapp.data.schema.ExamsSchema
import co.timetableapp.data.schema.SubjectsSchema
import co.timetableapp.model.Subject

class SubjectHandler(context: Context) : TimetableItemHandler<Subject>(context) {

    override val tableName = SubjectsSchema.TABLE_NAME

    override val itemIdCol = SubjectsSchema._ID

    override val timetableIdCol = SubjectsSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Subject.from(cursor)

    override fun createFromId(id: Int) = Subject.create(context, id)

    override fun propertiesAsContentValues(item: Subject): ContentValues {
        val values = ContentValues()
        with(values) {
            put(SubjectsSchema._ID, item.id)
            put(SubjectsSchema.COL_TIMETABLE_ID, item.timetableId)
            put(SubjectsSchema.COL_NAME, item.name)
            put(SubjectsSchema.COL_ABBREVIATION, item.abbreviation)
            put(SubjectsSchema.COL_COLOR_ID, item.colorId)
        }
        return values
    }

    override fun deleteItemWithReferences(itemId: Int) {
        super.deleteItemWithReferences(itemId)

        val classesQuery = Query.Builder()
                .addFilter(Filters.equal(ClassesSchema.COL_SUBJECT_ID, itemId.toString()))
                .build()

        val classUtils = ClassHandler(context)
        classUtils.getAllItems(classesQuery).forEach {
            classUtils.deleteItemWithReferences(it.id)
        }

        val examsQuery = Query.Builder()
                .addFilter(Filters.equal(ExamsSchema.COL_SUBJECT_ID, itemId.toString()))
                .build()

        val examUtils = ExamHandler(context)
        examUtils.getAllItems(examsQuery).forEach {
            examUtils.deleteItem(it.id)
        }
    }

}
