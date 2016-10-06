package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.ClassesSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper
import org.threeten.bp.LocalDate

class Class(val id: Int, val timetableId: Int, val subjectId: Int,
            val moduleName: String, val startDate: LocalDate,
            val endDate: LocalDate) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassesSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_TIMETABLE_ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_SUBJECT_ID)),
            cursor.getString(cursor.getColumnIndex(ClassesSchema.COL_MODULE_NAME)),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_DATE_DAY_OF_MONTH))),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_DATE_DAY_OF_MONTH))))

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

    fun hasModuleName() = moduleName.trim().length != 0

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

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<Class> = object : Parcelable.Creator<Class> {
            override fun createFromParcel(source: Parcel): Class = Class(source)
            override fun newArray(size: Int): Array<Class?> = arrayOfNulls(size)
        }

        @JvmField val NO_DATE: LocalDate = LocalDate.MIN

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
            val cls = Class(cursor)
            cursor.close()
            return cls
        }

        @JvmStatic
        fun makeName(cls: Class, subject: Subject) = if (cls.hasModuleName()) {
            "${subject.name}: ${cls.moduleName}"
        } else {
            subject.name
        }

    }
}
