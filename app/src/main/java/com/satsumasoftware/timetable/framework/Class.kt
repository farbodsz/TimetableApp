package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.TimetableDbHelper
import com.satsumasoftware.timetable.db.schema.ClassesSchema
import org.threeten.bp.LocalDate

/**
 * Represents a class the user would attend.
 *
 * Every class is part of a subject, which is why it includes a `subjectId`; in other words, a
 * subject is related to one or more classes. For example, the subject could be Mathematics,
 * and there could be different classes for the Statistics module and the Mechanics module.
 *
 * @property id the identifier for this class
 * @property timetableId the identifier of the [Timetable] this class is a part of
 * @property subjectId the identifier of the [Subject] this class is a part of
 * @property moduleName an optional name for the class' module
 * @property startDate the class' start date (optional)
 * @property endDate the class' end date (optional)
 */
class Class(val id: Int, val timetableId: Int, val subjectId: Int,
            val moduleName: String, val startDate: LocalDate,
            val endDate: LocalDate) : Parcelable {

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
         * @return the displayed name of the class, including the module name if it has one
         */
        @JvmStatic
        fun makeName(cls: Class, subject: Subject) = if (cls.hasModuleName()) {
            "${subject.name}: ${cls.moduleName}"
        } else {
            subject.name
        }

        /**
         * The field used if the class has no start/end dates
         */
        @JvmField val NO_DATE: LocalDate = LocalDate.MIN

        @Suppress("unused") @JvmField val CREATOR: Parcelable.Creator<Class> =
                object : Parcelable.Creator<Class> {
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

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeInt(subjectId)
        dest?.writeString(moduleName)
        dest?.writeSerializable(startDate)
        dest?.writeSerializable(endDate)
    }

}
