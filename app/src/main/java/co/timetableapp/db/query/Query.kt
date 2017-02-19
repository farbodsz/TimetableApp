package co.timetableapp.db.query

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
