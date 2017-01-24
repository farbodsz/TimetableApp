package com.satsumasoftware.timetable.licenses

import android.content.Context
import com.satsumasoftware.timetable.R

interface License : Comparable<License> {

    val name: String

    fun getNotice(context: Context): String {
        return context.resources.getString(R.string.license_apache_2_0)
    }

    override fun compareTo(other: License) = name.compareTo(other.name)

}
