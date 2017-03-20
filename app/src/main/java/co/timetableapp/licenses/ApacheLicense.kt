package co.timetableapp.licenses

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.R

class ApacheLicense() : License, Parcelable {

    override val name = "Apache License 2.0"

    override fun getNotice(context: Context): String {
        return context.getString(R.string.license_apache_2_0)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ApacheLicense> = object : Parcelable.Creator<ApacheLicense> {
            override fun createFromParcel(source: Parcel): ApacheLicense = ApacheLicense(source)
            override fun newArray(size: Int): Array<ApacheLicense?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {}
}
