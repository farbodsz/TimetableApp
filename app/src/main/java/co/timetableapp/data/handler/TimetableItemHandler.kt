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

import android.app.Application
import android.content.Context
import co.timetableapp.TimetableApplication
import co.timetableapp.data.query.Filters
import co.timetableapp.data.query.Query
import co.timetableapp.model.TimetableItem
import java.util.*

/**
 * This abstract data handler provides additional functionality only available for types that are
 * subclasses of [TimetableItem].
 */
abstract class TimetableItemHandler<T : TimetableItem>(context: Context) : DataHandler<T>(context) {

    /**
     * The column name of the column that contains the timetable ids in the table.
     */
    abstract val timetableIdCol: String

    /**
     * @return a list of all items of type [T] that belong to the current timetable.
     */
    fun getItems(application: Application): ArrayList<T> {
        val timetable = (application as TimetableApplication).currentTimetable!!

        val query = Query.Builder()
                .addFilter(Filters.equal(timetableIdCol, timetable.id.toString()))
                .build()

        return getAllItems(query)
    }

}
