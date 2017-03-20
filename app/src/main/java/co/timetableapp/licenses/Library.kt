package co.timetableapp.licenses

import android.os.Parcel
import android.os.Parcelable

class Library(val name: String, val website: String?, val author: String,
              val license: License) : Comparable<Library>, Parcelable {

    override fun compareTo(other: Library) = name.compareTo(other.name)

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Library> = object : Parcelable.Creator<Library> {
            override fun createFromParcel(source: Parcel): Library = Library(source)
            override fun newArray(size: Int): Array<Library?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readParcelable<License>(License::class.java.classLoader))

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(name)
        dest?.writeString(website)
        dest?.writeString(author)
        dest?.writeParcelable(license, 0)
    }
}
