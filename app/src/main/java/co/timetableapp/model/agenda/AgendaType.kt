package co.timetableapp.model.agenda

/**
 * Contains the kinds of agenda items that can be used.
 *
 * These are values effectively representing the subclasses of [AgendaItem].
 */
enum class AgendaType {
    ASSIGNMENT,
    EXAM,
    EVENT
}
