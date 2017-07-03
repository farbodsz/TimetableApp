package co.timetableapp.ui.agenda

import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.model.agenda.AgendaItem
import java.util.*

/**
 * A class to help with storing filter parameters when displaying items in [AgendaListFragment].
 *
 * @property typesToShow    the types being displayed in the list
 * @property showPast       whether or not past items will be shown
 */
class AgendaFilterParams(
        val typesToShow: EnumSet<AgendaItem.Types>,
        var showCompleted: Boolean,
        var showPast: Boolean
) : Parcelable {

    constructor(source: Parcel) : this(
            source.readSerializable() as EnumSet<AgendaItem.Types>,
            source.readInt() == 1,
            source.readInt() == 1
    )

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AgendaFilterParams> = object : Parcelable.Creator<AgendaFilterParams> {
            override fun createFromParcel(source: Parcel): AgendaFilterParams = AgendaFilterParams(source)
            override fun newArray(size: Int): Array<AgendaFilterParams?> = arrayOfNulls(size)
        }
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(typesToShow)
        dest.writeInt(if (showCompleted) 1 else 0)
        dest.writeInt(if (showPast) 1 else 0)
    }

}
