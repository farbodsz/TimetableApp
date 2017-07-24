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

    private constructor(source: Parcel) : this()

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {}
}
