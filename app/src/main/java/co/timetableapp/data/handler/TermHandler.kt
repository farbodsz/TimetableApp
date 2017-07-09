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
import co.timetableapp.data.schema.TermsSchema
import co.timetableapp.model.Term

class TermHandler(context: Context) : TimetableItemHandler<Term>(context) {

    override val tableName = TermsSchema.TABLE_NAME

    override val itemIdCol = TermsSchema._ID

    override val timetableIdCol = TermsSchema.COL_TIMETABLE_ID

    override fun createFromCursor(cursor: Cursor) = Term.from(cursor)

    override fun createFromId(id: Int) = Term.create(context, id)

    override fun propertiesAsContentValues(item: Term): ContentValues {
        val values = ContentValues()
        with(values) {
            put(TermsSchema._ID, item.id)
            put(TermsSchema.COL_TIMETABLE_ID, item.timetableId)
            put(TermsSchema.COL_NAME, item.name)
            put(TermsSchema.COL_START_DATE_DAY_OF_MONTH, item.startDate.dayOfMonth)
            put(TermsSchema.COL_START_DATE_MONTH, item.startDate.monthValue)
            put(TermsSchema.COL_START_DATE_YEAR, item.startDate.year)
            put(TermsSchema.COL_END_DATE_DAY_OF_MONTH, item.endDate.dayOfMonth)
            put(TermsSchema.COL_END_DATE_MONTH, item.endDate.monthValue)
            put(TermsSchema.COL_END_DATE_YEAR, item.endDate.year)
        }
        return values
    }

}
