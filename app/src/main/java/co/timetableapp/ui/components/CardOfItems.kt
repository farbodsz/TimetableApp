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
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.ui.components.CardOfItems.Builder

/**
 * This class can be used to create a View of a card displaying a list of sub-items inside.
 *
 * Specifically, the layout will include a title at the top of the card, then a list of items below
 * it. At the bottom of the card, there is a button to view.
 *
 * An instance of this class must be constructed using the [Builder].
 */
@Suppress("unused")
class CardOfItems private constructor(val view: View) {

    /**
     * Represents a sub-item that would be displayed on this card component.
     *
     * @param title     the item's title
     * @param subtitle  the item's subtitle. This may be encapsulated by HTML via
     *                  [android.text.Html.fromHtml] hence this parameter is a [Spanned] type
     */
    class CardItem(
            val title: String,
            val subtitle: Spanned
    )

    /**
     * A builder to create a [CardOfItems].
     *
     * If a title is not specified (using [setTitle]), it will not be shown on the card.
     * Similarly, if button properties are not specified with [setButtonProperties], then a button
     * will not be shown on the card.
     */
    class Builder(private val mContext: Context, private val mRoot: ViewGroup) {

        private var mTitle: String? = null
        private var mItems: ArrayList<CardItem>? = null
        @DrawableRes private var mItemsIconRes: Int? = null
        private var mButtonText: String? = null
        private var mButtonClickListener: View.OnClickListener? = null

        fun setTitle(title: String): Builder {
            mTitle = title
            return this
        }

        fun setTitle(@StringRes titleRes: Int): Builder {
            mTitle = mContext.getString(titleRes)
            return this
        }

        fun setItems(items: ArrayList<CardItem>): Builder {
            mItems = items
            return this
        }

        /**
         * Sets the drawable to use as the icon for all items displayed in the list on the card.
         */
        fun setItemsIconResource(@DrawableRes drawableRes: Int): Builder {
            mItemsIconRes = drawableRes
            return this
        }

        fun setButtonProperties(buttonText: String,
                                onClickListener: View.OnClickListener): Builder {
            mButtonText = buttonText
            mButtonClickListener = onClickListener
            return this
        }

        fun setButtonProperties(@StringRes buttonTextRes: Int,
                                onClickListener: View.OnClickListener): Builder {
            setButtonProperties(mContext.getString(buttonTextRes), onClickListener)
            return this
        }

        fun build(): CardOfItems {
            val inflater = LayoutInflater.from(mContext)

            val card = inflater.inflate(R.layout.item_card_of_items, mRoot, false)

            with(card) {
                val titleView = findViewById<TextView>(R.id.title)
                if (mTitle == null) {
                    titleView.visibility = View.GONE
                } else {
                    titleView.text = mTitle
                }

                if (mItems != null && mItems!!.isNotEmpty()) {
                    val container = findViewById<LinearLayout>(R.id.container)

                    container.removeAllViews() // by default contains a placeholder text
                    populateCardContainer(container, mItems!!)
                }

                val button = findViewById<Button>(R.id.button)
                if (mButtonText == null) {
                    button.visibility = View.GONE
                } else {
                    button.text = mButtonText
                    button.setOnClickListener(mButtonClickListener)
                }
            }

            return CardOfItems(card)
        }

        /**
         * Populates the container with the list of items to display on the card
         */
        private fun populateCardContainer(container: LinearLayout, items: ArrayList<CardItem>) {
            items.forEach {
                val itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.item_condensed, container, false)

                with(itemView) {
                    findViewById<TextView>(R.id.title).text = it.title
                    findViewById<TextView>(R.id.subtitle).text = it.subtitle

                    val imageView = findViewById<ImageView>(R.id.icon)
                    mItemsIconRes?.let {
                        imageView.setImageResource(it)
                    }
                }

                container.addView(itemView)
            }
        }
    }

}
