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

package co.timetableapp.data.query

import android.util.Log
import java.util.*

/**
 * Used to filter results when performing SQL queries on the database
 *
 * @see Filter
 */
class Query(val filter: Filter) {

    class Builder {

        companion object {
            @JvmField val LOG_TAG = "Query.Builder"
        }

        private val filters: ArrayList<Filter> = ArrayList()

        fun addFilter(filter: Filter): Builder {
            filters.add(filter)
            Log.v(LOG_TAG, "Added filter: ${filter.sqlStatement}")
            return this
        }

        private fun combineFilters(): Filter {
            when (filters.size) {
                0 -> throw IllegalStateException("you must add at least one filter to the " +
                        "query builder")
                1 -> return filters[0]
                2 -> return Filters.and(filters[0], filters[1])
                else -> {
                    // We need to make an array excluding the first two elements for the vararg
                    val moreFilters = filters.slice(IntRange(2, filters.size - 1)).toTypedArray()

                    return Filters.and(filters[0], filters[1], *moreFilters)
                }
            }
        }

        fun build(): Query {
            return Query(combineFilters())
        }

    }
}
