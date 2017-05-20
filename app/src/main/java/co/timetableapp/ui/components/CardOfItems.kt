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
                val titleView = findViewById(R.id.title) as TextView
                if (mTitle == null) {
                    titleView.visibility = View.GONE
                } else {
                    titleView.text = mTitle
                }

                if (mItems != null && mItems!!.isNotEmpty()) {
                    val container = findViewById(R.id.container) as LinearLayout

                    container.removeAllViews() // by default contains a placeholder text
                    populateCardContainer(container, mItems!!)
                }

                val button = findViewById(R.id.button) as Button
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
                    (findViewById(R.id.title) as TextView).text = it.title
                    (findViewById(R.id.subtitle) as TextView).text = it.subtitle

                    val imageView = findViewById(R.id.icon) as ImageView
                    mItemsIconRes?.let {
                        imageView.setImageResource(it)
                    }
                }

                container.addView(itemView)
            }
        }
    }

}
