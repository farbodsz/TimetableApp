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

package co.timetableapp.model

import android.os.Parcelable

/**
 * All data model classes should implement this
 */
interface BaseItem : Parcelable {

    /**
     * The integer identifier used to represent the data item in the database.
     */
    val id: Int

    /**
     * Specifies a sorting order based on the integer identifiers of a [BaseItem].
     * They would be sorted from lowest id to highest id.
     *
     * @see id
     */
    class ItemIdComparator : Comparator<BaseItem> {

        override fun compare(o1: BaseItem?, o2: BaseItem?) = o1!!.id.compareTo(o2!!.id)
    }

}
