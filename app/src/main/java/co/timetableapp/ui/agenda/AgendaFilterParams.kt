/*
 * Copyright 2017 Farbod Salamat-Zadeh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.timetableapp.ui.agenda

import android.os.Parcel
import android.os.Parcelable
import co.timetableapp.model.agenda.AgendaType
import java.util.*

/**
 * A class to help with storing filter parameters when displaying items in [AgendaFragment].
 *
 * @property typesToShow    the types being displayed in the list
 * @property showCompleted  true if completed items are being shown in the list UI
 * @property showPast       true if only past items are being shown in the list UI
 */
class AgendaFilterParams(
        var typesToShow: EnumSet<AgendaType>,
        var showCompleted: Boolean,
        var showPast: Boolean
) : Parcelable {

    private constructor(source: Parcel) : this(
            source.readSerializable() as EnumSet<AgendaType>,
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
