package co.timetableapp.model

/**
 * This should be implemented by any data model that is uses a date.
 */
interface DateItem {

    /**
     * Whether the item's date was in the past
     *
     * @see isUpcoming
     */
    fun isInPast(): Boolean

    /**
     * If the item's date is in the future
     *
     * @see isInPast
     */
    fun isUpcoming() = !isInPast()

}
