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

/**
 * Data classes that are part of a timetable should implement this.
 */
interface TimetableItem : BaseItem {


    /**
     * The integer identifier for the ([Timetable]) this data item is part of.
     *
     * This is used to filter the items for the user's currently selected timetable.
     */
    val timetableId: Int

}
