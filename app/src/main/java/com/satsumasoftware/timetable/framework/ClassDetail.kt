package com.satsumasoftware.timetable.framework

import android.os.Parcel
import android.os.Parcelable

import java.util.*

class ClassDetail(val id: Int, val room: String, val teacher: String,
                  val classTimeIds: ArrayList<Int>) : Parcelable {

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readString(),
            ArrayList<Int>().apply{ source.readList(this, Int::class.java.classLoader) })

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(room)
        dest?.writeString(teacher)
        dest?.writeList(classTimeIds)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ClassDetail> = object : Parcelable.Creator<ClassDetail> {
            override fun createFromParcel(source: Parcel): ClassDetail = ClassDetail(source)
            override fun newArray(size: Int): Array<ClassDetail?> = arrayOfNulls(size)
        }
    }
}
