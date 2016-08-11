package com.satsumasoftware.timetable.framework

import android.content.Context
import android.database.Cursor
import java.util.*
import android.os.Parcel
import android.os.Parcelable
import com.satsumasoftware.timetable.db.ClassesSchema
import com.satsumasoftware.timetable.db.ClassesUtils

class Class(val id: Int, val subjectId: Int, val classDetailIds: ArrayList<Int>) : Parcelable {

    constructor(context: Context, cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassesSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_SUBJECT_ID)),
            ClassesUtils.getClassDetailIds(
                    context, cursor.getInt(cursor.getColumnIndex(ClassesSchema._ID))))

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readInt(),
            ArrayList<Int>().apply{ source.readList(this, Int::class.java.classLoader) })

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeInt(subjectId)
        dest?.writeList(classDetailIds)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Class> = object : Parcelable.Creator<Class> {
            override fun createFromParcel(source: Parcel): Class = Class(source)
            override fun newArray(size: Int): Array<Class?> = arrayOfNulls(size)
        }
    }
}
