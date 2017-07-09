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

/**
 * A collection of static helper functions for creating or combining filters.
 *
 * @see Filter
 * @see Query
 */
object Filters {

    @JvmStatic
    fun equal(property: String, value: String)= Filter("$property = $value")

    @JvmStatic
    fun and(filter1: Filter, filter2: Filter, vararg moreFilters: Filter) =
            joinFilters("AND", filter1, filter2, *moreFilters)

    @JvmStatic
    fun or(filter1: Filter, filter2: Filter, vararg moreFilters: Filter) =
            joinFilters("OR", filter1, filter2, *moreFilters)

    @JvmStatic
    private fun joinFilters(sqlKeyword: String, filter1: Filter, filter2: Filter,
                            vararg moreFilters: Filter): Filter {
        var sql: String = "${filter1.sqlStatement} $sqlKeyword ${filter2.sqlStatement}"

        for ((sqlStatement) in moreFilters) {
            sql = "$sql $sqlKeyword $sqlStatement"
        }

        return Filter(sql)
    }

}
