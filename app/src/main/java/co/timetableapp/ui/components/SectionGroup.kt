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

package co.timetableapp.ui.components

import android.content.Context
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.ui.components.SectionGroup.Builder

/**
 * This class represents a section in the user interface and is used to generate the interface
 * dynamically.
 * The [Builder] class must be used to create a [SectionGroup].
 */
@Suppress("unused")
class SectionGroup private constructor(val view: View) {

    /**
     * @return the [FrameLayout] containing all the views for the header of the section.
     */
    val headerView: FrameLayout = view.findViewById(R.id.header) as FrameLayout

    /**
     * @return the empty [LinearLayout] of the section which views should be added to dynamically.
     */
    val containerView: LinearLayout = view.findViewById(R.id.container) as LinearLayout

    /**
     * This builder class must be used to create a [SectionGroup].
     * Builder methods can be used to customize the header of the section.
     */
    class Builder(private val mContext: Context, private val mRoot: ViewGroup) {

        private var mTitle: String? = null
        private var mButtonText: String? = null
        private var mButtonIsBorderless = false

        fun setTitle(title: String): Builder {
            mTitle = title
            return this
        }

        fun setTitle(@StringRes titleRes: Int): Builder {
            mTitle = mContext.getString(titleRes)
            return this
        }

        fun setButtonText(buttonText: String?): Builder {
            mButtonText = buttonText
            return this
        }

        fun setButtonText(@StringRes buttonTextRes: Int): Builder {
            mButtonText = mContext.getString(buttonTextRes)
            return this
        }

        fun setButtonBorderless(isBorderless: Boolean): Builder {
            mButtonIsBorderless = isBorderless
            return this
        }

        fun build(): SectionGroup {
            val inflater = LayoutInflater.from(mContext)

            val sectionHeader = inflater.inflate(R.layout.item_section, mRoot, false)

            with(sectionHeader) {
                mTitle?.let {
                    with(findViewById(R.id.title) as TextView) {
                        visibility = View.VISIBLE
                        text = mTitle
                    }
                }


                mButtonText?.let {
                    val buttonResId =
                            if (mButtonIsBorderless) R.id.button_borderless else R.id.button

                    with(findViewById(buttonResId) as TextView) {
                        visibility = View.VISIBLE
                        text = mButtonText
                    }
                }
            }

            return SectionGroup(sectionHeader)
        }
    }

}
