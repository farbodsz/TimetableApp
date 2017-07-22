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

package co.timetableapp.model.home

import co.timetableapp.model.Color

/**
 * Contains the strings to be displayed on a list item on the home page.
 */
interface HomeItemProperties {

    /**
     * The item's title.
     */
    val title: String

    /**
     * The item's subtitle.
     * This is optional and should be null if the item does not display a subtitle.
     */
    val subtitle: String?

    /**
     * The date or time of the item.
     */
    val time: String

    /**
     * A very short additional text that can be displayed with the item.
     * This is optional and should be null if the item does not display this additional text.
     */
    val extraText: String?

    /**
     * An associated color with the item.
     */
    val color: Color

}
