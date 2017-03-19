package com.satsumasoftware.timetable.data.query

/**
 * A collection of static helper functions for creating or combining filters.
 *
 * @see Filter
 * @see Query
 */
object Filters {

    @JvmStatic
    fun equal(property: String, value: String): Filter {
        return Filter("$property = $value")
    }

    @JvmStatic
    fun lessOrEqualThan(property: String, value: String): Filter {
        return Filter("$property <= $value")
    }

    @JvmStatic
    fun moreOrEqualThan(property: String, value: String): Filter {
        return Filter("$property >= $value")
    }

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

        for (filter in moreFilters) {
            sql = "$sql $sqlKeyword ${filter.sqlStatement}"
        }

        return Filter(sql)
    }

}
