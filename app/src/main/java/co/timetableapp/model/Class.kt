package co.timetableapp.model

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.data.TimetableDbHelper
import co.timetableapp.data.schema.ClassesSchema
import org.threeten.bp.LocalDate

/**
 * Represents a class the user would attend.
 *
 * Every class is part of a subject, which is why it includes a `subjectId`; in other words, a
 * subject is related to one or more classes. For example, the subject could be Mathematics,
 * and there could be different classes for the Statistics module and the Mechanics module.
 *
 * @property subjectId the identifier of the [Subject] this class is a part of
 * @property moduleName an optional name for the class' module
 * @property startDate the class' start date (optional)
 * @property endDate the class' end date (optional)
 */
data class Class(
        override val id: Int,
        override val timetableId: Int,
        val subjectId: Int,
        val moduleName: String,
        val startDate: LocalDate,
        val endDate: LocalDate
) : TimetableItem {

    init {
        if (hasStartEndDates() && startDate.isAfter(endDate)) {
            throw IllegalArgumentException("the start date cannot be after the end date")
        }
    }

    companion object {

        /**
         * Constructs a [Class] using column values from the cursor provided
         *
         * @param cursor a query of the classes table
         * @see [ClassesSchema]
         */
        @JvmStatic
        fun from(cursor: Cursor): Class {
            val startDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_DATE_DAY_OF_MONTH)))
            val endDate = LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_DATE_DAY_OF_MONTH)))

            return Class(cursor.getInt(cursor.getColumnIndex(ClassesSchema._ID)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_TIMETABLE_ID)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_SUBJECT_ID)),
                    cursor.getString(cursor.getColumnIndex(ClassesSchema.COL_MODULE_NAME)),
                    startDate,
                    endDate)
        }

        @JvmStatic
        fun create(context: Context, classId: Int): Class? {
            val dbHelper = TimetableDbHelper.getInstance(context)
            val cursor = dbHelper.readableDatabase.query(
                    ClassesSchema.TABLE_NAME,
                    null,
                    "${ClassesSchema._ID}=?",
                    arrayOf(classId.toString()),
                    null, null, null)
            cursor.moveToFirst()
            if (cursor.count == 0) {
                cursor.close()
                return null
            }
            val cls = Class.from(cursor)
            cursor.close()
            return cls
        }

        /**
         * The field used if the class has no start/end dates
         */
        @JvmField val NO_DATE: LocalDate = LocalDate.MIN

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Class> = object : Parcelable.Creator<Class> {
            override fun createFromParcel(source: Parcel): Class = Class(source)
            override fun newArray(size: Int): Array<Class?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString(),
            source.readSerializable() as LocalDate,
            source.readSerializable() as LocalDate)

    init {
        if (startDate == NO_DATE && endDate != NO_DATE ||
                endDate == NO_DATE && startDate != NO_DATE) {
            throw IllegalStateException("either startDate or endDate has values [0,0,0] but the " +
                    "other doesn't - startDate and endDate must both be the same state")
        }
    }

    fun hasModuleName() = moduleName.trim().isNotEmpty()

    fun hasStartEndDates() = startDate != NO_DATE && endDate != NO_DATE

    /**
     * @return false if the class' start and end dates are in the past or the future. This would be
     * used when displaying a list of classes - most of the time, the user would only want to see
     * their current classes.
     *
     * @see startDate
     * @see endDate
     */
    fun isCurrent() = if (hasStartEndDates()) {
        !startDate.isAfter(LocalDate.now()) && !endDate.isBefore(LocalDate.now())
    } else {
        true
    }

    /**
     * @return the displayed name of the class, including the module name if it has one
     */
    fun makeName(subject: Subject) = if (hasModuleName()) {
        "${subject.name}: $moduleName"
    } else {
        subject.name
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeInt(subjectId)
        dest?.writeString(moduleName)
        dest?.writeSerializable(startDate)
        dest?.writeSerializable(endDate)
    }

    /**
     * Defines the natural sorting order for classes.
     * This could not be implemented using [Comparable] since it requires context.
     */
    class NaturalSortComparator(private val context: Context) : Comparator<Class> {

        override fun compare(o1: Class?, o2: Class?): Int {
            val subject1 = Subject.create(context, o1!!.subjectId)!!
            val subject2 = Subject.create(context, o2!!.subjectId)!!
            return subject1.compareTo(subject2)
        }
    }

}
