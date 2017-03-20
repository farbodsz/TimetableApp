package com.satsumasoftware.timetable.util

import android.content.Context
import android.support.annotation.StringRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.util.SectionUi.Builder

/**
 * This class represents a section in the user interface and is used to generate the interface
 * dynamically.
 * The [Builder] class must be used to create a [SectionUi].
 */
@Suppress("unused")
class SectionUi private constructor(val view: View) {

    /**
     * @return the [FrameLayout] containing all the views for the header of the section.
     */
    val headerView: FrameLayout = view.findViewById(R.id.header) as FrameLayout

    /**
     * @return the empty [LinearLayout] of the section which views should be added to dynamically.
     */
    val containerView: LinearLayout = view.findViewById(R.id.container) as LinearLayout

    /**
     * This builder class must be used to create a [SectionUi].
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

        fun build(): SectionUi {
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

            return SectionUi(sectionHeader)
        }
    }

}
