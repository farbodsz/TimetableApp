package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.ClassesSchema
import com.satsumasoftware.timetable.db.TimetableDbHelper

class Class(val id: Int, val timetableId: Int, val subjectId: Int,
            val moduleName: String) : Parcelable {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassesSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_TIMETABLE_ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_SUBJECT_ID)),
            cursor.getString(cursor.getColumnIndex(ClassesSchema.COL_MODULE_NAME)))

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            source.readInt(),
            source.readString())

    fun hasModuleName() = moduleName.trim().length != 0

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(timetableId)
        dest?.writeInt(subjectId)
        dest?.writeString(moduleName)
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<Class> = object : Parcelable.Creator<Class> {
            override fun createFromParcel(source: Parcel): Class = Class(source)
            override fun newArray(size: Int): Array<Class?> = arrayOfNulls(size)
        }

        @JvmStatic fun create(context: Context, classId: Int): Class? {
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

        @JvmStatic fun makeName(cls: Class, subject: Subject) = if (cls.hasModuleName()) {
            "${subject.name}: ${cls.moduleName}"
        } else {
            subject.name
        }

    }
}
