package co.timetableapp.model

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.handler.DataNotFoundException
import co.timetableapp.data.schema.AssignmentsSchema
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Represents an assignment the user may have been given.
 *
 * @property classId the identifier of the [Class] this assignment is associated with
 * @property title the name of the assignment
 * @property detail optional, additional notes the user may enter for the assignment
 * @property dueDate the date the assignment must be handed in
 * @property completionProgress an integer from 0-100 (like a percentage) indicating how complete
 *      the assignment is (100 indicating fully complete)
 */
data class Assignment(
        override val id: Int,
        override val timetableId: Int,
        val classId: Int,
        val title: String,
        val detail: String,
        val dueDate: LocalDate,
        var completionProgress: Int
) : TimetableItem, AgendaItem, DateItem {

    init {
        if (completionProgress !in 0..100) {
            throw IllegalArgumentException("the completion progress must be between 0 and 100")
        }
    }

    companion object {

        /**
         * @see ReverseDueDateComparator
         */
        @JvmField val COMPARATOR_REVERSE_DUE_DATE = ReverseDueDateComparator()

        /**
         * Constructs an [Assignment] using column values from the cursor provided
         *
         * @param cursor a query of the assignments table
         * @see [AssignmentsSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Assignment {
            val dueDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH)))

            return Assignment(
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_TIMETABLE_ID)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_CLASS_ID)),
                    cursor.getString(cursor.getColumnIndex(AssignmentsSchema.COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(AssignmentsSchema.COL_DETAIL)),
                    dueDate,
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_COMPLETION_PROGRESS)))
        }

        /**
         * Creates an [Assignment] from the [assignmentId] and corresponding data in the database.
         *
         * @throws DataNotFoundException if the database query returns no results
         * @see from
         */
        @JvmStatic
        @Throws(DataNotFoundException::class)
        fun create(context: Context, assignmentId: Int): Assignment {
            val db = TimetableDbHelper.getInstance(context).readableDatabase
            val cursor = db.query(
                    AssignmentsSchema.TABLE_NAME,
                    null,
                    "${AssignmentsSchema._ID}=?",
                    arrayOf(assignmentId.toString()),
                    null, null, null)

            if (cursor.count == 0) {
                cursor.close()
                throw DataNotFoundException(this::class.java, assignmentId)
            }

            cursor.moveToFirst()
            val assignment = Assignment.from(cursor)
            cursor.close()
            return assignment
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Assignment> = object : Parcelable.Creator<Assignment> {
            override fun createFromParcel(source: Parcel): Assignment = Assignment(source)
            override fun newArray(size: Int): Array<Assignment?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readInt())

    fun hasDetail() = detail.trim().isNotEmpty()

    fun isComplete() = completionProgress == 100

    fun isOverdue() = !isComplete() && isInPast()

    override fun isInPast() = dueDate.isBefore(LocalDate.now())

    fun isPastAndDone() = isInPast() && isComplete()

    override fun getDisplayedTitle() = title

    override fun getRelatedSubject(context: Context): Subject? {
        val cls = Class.create(context, classId)
        return Subject.create(context, cls.subjectId)
    }

    override fun getDateTime() = LocalDateTime.of(dueDate, LocalTime.MIDNIGHT)!!

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeInt(classId)
        dest?.writeString(title)
        dest?.writeString(detail)
        dest?.writeSerializable(dueDate)
        dest?.writeInt(completionProgress)
    }

    /**
     * Defines a sorting order for assignments, first being sorted in reverse by date (so that when
     * viewing past assignments, the most recent is shown first), then lexicographically.
     */
    class ReverseDueDateComparator : Comparator<Assignment> {

        override fun compare(o1: Assignment?, o2: Assignment?): Int {
            // Sorting order is reverse due dates then titles
            val dateComparison = o2!!.dueDate.compareTo(o1!!.dueDate)
            return if (dateComparison == 0) {
                o1.title.compareTo(o2.title)
            } else {
                dateComparison
            }
        }
    }
  
}
