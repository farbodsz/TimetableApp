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

import co.timetableapp.ui.OnItemClick

/**
 * Represents a header shown on the home page.
 *
 * @param name      the title of the header
 * @param onClick   the action to be performed if the header is clicked. This can be null if there
 *                  is no action.
 */
class HomeHeader(val name: String, val onClick: OnItemClick?) : HomeListItem {
    constructor(name: String) : this(name, null)
}
