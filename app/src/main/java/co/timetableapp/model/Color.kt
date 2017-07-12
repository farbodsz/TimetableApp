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

import android.content.Context
import android.content.res.Resources
import android.support.annotation.ColorRes

/**
 * Represents a color from the material design spec.
 *
 * See the material design
 * [color palette](https://material.google.com/style/color.html#color-color-palette) for details.
 *
 * @property id An identifier used to represent one of 19 material design colors.
 */
data class Color(val id: Int) {

    companion object {

        /**
         * @return a list of all possible colors
         */
        @JvmStatic
        fun getAllColors(): ArrayList<Color> {
            val colors = ArrayList<Color>()
            (1..19).mapTo(colors) { Color(it) }
            return colors
        }
    }

    // TODO sealed classes?

    val name: String
        get() = when(id) {
            1 -> "red"
            2 -> "pink"
            3 -> "purple"
            4 -> "deep_purple"
            5 -> "indigo"
            6 -> "blue"
            7 -> "light_blue"
            8 -> "cyan"
            9 -> "teal"
            10 -> "green"
            11 -> "light_green"
            12 -> "lime"
            13 -> "yellow"
            14 -> "amber"
            15 -> "orange"
            16 -> "deep_orange"
            17 -> "brown"
            18 -> "grey"
            19 -> "blue_grey"
            else -> throw IllegalArgumentException("invalid color id '$id'")
        }

    @ColorRes
    fun getLightAccentColorRes(context: Context) = getColor(context, "mdu_${name}_300")

    @ColorRes
    fun getPrimaryColorResId(context: Context) = getColor(context, "mdu_${name}_500")

    @ColorRes
    fun getPrimaryDarkColorResId(context: Context) = getColor(context, "mdu_${name}_700")

    @ColorRes
    private fun getColor(context: Context, resName: String) = try {
        context.resources.getIdentifier(resName, "color", context.packageName)
    } catch (e: Resources.NotFoundException) {
        e.printStackTrace()
        0
    }

}
