package com.satsumasoftware.timetable.licenses

import android.content.Context
import com.satsumasoftware.timetable.R

class ApacheLicense : License {

    override val name = "Apache License 2.0"

    override fun getNotice(context: Context): String {
        return context.getString(R.string.license_apache_2_0)
    }

}
