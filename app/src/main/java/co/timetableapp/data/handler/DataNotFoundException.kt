package co.timetableapp.data.handler

/**
 * A [DataNotFoundException] is thrown when an item cannot be found in the database.
 *
 * @param dataClass the type of item that cannot be found
 * @param itemId    the integer identifier of the item that cannot be found
 */
class DataNotFoundException(
        dataClass: Class<*>,
        itemId: Int
) : Exception("Could not find $dataClass with id: $itemId")
