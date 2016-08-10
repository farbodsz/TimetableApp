package com.satsumasoftware.timetable.framework

import java.util.*
import android.os.Parcel
import android.os.Parcelable

class Subject(val id: Int, var name: String) : Parcelable {

    constructor(source: Parcel): this(source.readInt(), source.readString())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeInt(id)
        dest?.writeString(name)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Subject> = object : Parcelable.Creator<Subject> {
            override fun createFromParcel(source: Parcel): Subject = Subject(source)
            override fun newArray(size: Int): Array<Subject?> = arrayOfNulls(size)
        }
    }
}
