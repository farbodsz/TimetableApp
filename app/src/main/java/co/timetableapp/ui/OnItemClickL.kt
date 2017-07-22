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

package co.timetableapp.ui

import android.view.View

/**
 * Type definition for an action to be performed when a view in the list has been clicked.
 *
 * This is a function type with its parameters as the view that was clicked and the layout position
 * of the ViewHolder. The function does not return anything.
 */
typealias OnItemClick = (view: View, position: Int) -> Unit
