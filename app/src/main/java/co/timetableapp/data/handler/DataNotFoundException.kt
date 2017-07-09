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

package co.timetableapp.data.handler

/**
 * A [DataNotFoundException] is thrown when an item cannot be found in the database.
 *
 * @param dataClass the type of item that cannot be found
 * @param itemId    the integer identifier of the item that cannot be found
 */
class DataNotFoundException(
        dataClass: Class<*>,
        itemId: Int
) : Exception("Could not find $dataClass with id: $itemId")
