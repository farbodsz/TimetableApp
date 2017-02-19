package co.timetableapp.db.query

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
    fun and(filter1: Filter, filter2: Filter, vararg moreFilters: Filter): Filter {
        var sql: String = "${filter1.sqlStatement} AND ${filter2.sqlStatement}"

        for (filter in moreFilters) {
            sql = "$sql AND ${filter.sqlStatement}"
        }

        return Filter(sql)
    }

}
