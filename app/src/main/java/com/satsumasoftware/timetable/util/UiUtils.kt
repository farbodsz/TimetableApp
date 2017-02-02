package com.satsumasoftware.timetable.util

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.framework.Color

class UiUtils private constructor() {

    companion object {

        @JvmStatic
        fun setBarColors(color: Color, activity: Activity, vararg views: View) {
            for (view in views) {
                view.setBackgroundColor(ContextCompat.getColor(activity, color.getPrimaryColorResId(activity)))
            }
            if (Build.VERSION.SDK_INT >= 21) {
                activity.window.statusBarColor =
                        ContextCompat.getColor(activity, color.getPrimaryDarkColorResId(activity))
            }
        }

        @JvmOverloads
        @JvmStatic
        fun tintDrawable(context: Context, @DrawableRes drawableRes: Int,
                         @ColorRes colorRes: Int = R.color.mdu_white): Drawable {
            val vectorDrawableCompat = VectorDrawableCompat.create(context.resources, drawableRes, null)
            val drawable = DrawableCompat.wrap(vectorDrawableCompat!!.current)
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, colorRes))
            return drawable
        }

        @JvmStatic
        fun tintMenuIcons(context: Context, menu: Menu, vararg @IdRes menuItems: Int) {
            menuItems.forEach {
                val icon = menu.findItem(it).icon
                icon?.let {
                    tintMenuDrawableWhite(context, icon)
                }
            }
        }

        private fun tintMenuDrawableWhite(context: Context, d: Drawable): Drawable {
            val drawable = DrawableCompat.wrap(d)
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.mdu_white))
            return drawable
        }

        @JvmOverloads
        @JvmStatic
        fun makePlaceholderView(context: Context,
                                @DrawableRes drawableRes: Int,
                                @StringRes titleRes: Int,
                                @ColorRes backgroundColorRes: Int = R.color.mdu_grey_50,
                                @ColorRes drawableColorRes: Int = R.color.mdu_grey_700,
                                @ColorRes textColorRes: Int = R.color.mdu_text_black_secondary,
                                largeIcon: Boolean = false,
                                @StringRes subtitleRes: Int? = null): View {
            val placeholderView = LayoutInflater.from(context).inflate(R.layout.placeholder, null)

            val background = placeholderView.findViewById(R.id.background)
            background.setBackgroundColor(ContextCompat.getColor(context, backgroundColorRes))

            val image = placeholderView.findViewById(R.id.imageView) as ImageView
            image.setImageDrawable(tintDrawable(context, drawableRes, drawableColorRes))
            if (largeIcon) {
                image.layoutParams.width = dpToPixels(context, 108)
                image.layoutParams.height = dpToPixels(context, 108)
            }

            val title = placeholderView.findViewById(R.id.title) as TextView
            title.setText(titleRes)
            title.setTextColor(ContextCompat.getColor(context, textColorRes))

            val subtitle = placeholderView.findViewById(R.id.subtitle) as TextView
            if (subtitleRes == null) {
                subtitle.visibility = View.GONE
            } else {
                subtitle.setText(subtitleRes)
                subtitle.setTextColor(ContextCompat.getColor(context, textColorRes))
            }

            return placeholderView
        }

        @JvmStatic
        fun dpToPixels(context: Context, dps: Int): Int {
            val density = context.resources.displayMetrics.density
            val dpAsPixels = (dps * density + 0.5f).toInt()
            return dpAsPixels
        }

        @JvmStatic
        fun isApi21() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    }
}
