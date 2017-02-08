package com.satsumasoftware.timetable.query

/**
 * Can store part of a SQL selection statement, used to filter results in database queries.
 *
 * @see Filters
 */
data class Filter(var sqlStatement: String) {

    init {
        // Surround the SQL command in parenthesis to avoid mix ups when combined with other
        // filters.
        sqlStatement = "($sqlStatement)"
    }

}
