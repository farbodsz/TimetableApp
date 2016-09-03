package com.satsumasoftware.timetable.util

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.satsumasoftware.timetable.R
import com.satsumasoftware.timetable.framework.Color

class ThemeUtils {

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
            return tintDrawable(context, ContextCompat.getDrawable(context, drawableRes), colorRes)
        }

        @JvmOverloads
        @JvmStatic
        fun tintDrawable(context: Context, d: Drawable, @ColorRes colorRes: Int = R.color.mdu_white): Drawable {
            val drawable = DrawableCompat.wrap(d)
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, colorRes))
            return drawable
        }

        @JvmStatic
        fun tintMenuIcons(context: Context, menu: Menu, vararg @IdRes menuItems: Int) {
            for (@IdRes menuItem in menuItems) {
                val icon = menu.findItem(menuItem).icon
                icon?.let { tintDrawable(context, icon) }
            }
        }


        @JvmOverloads
        @JvmStatic
        fun makePlaceholderView(context: Context,
                                @DrawableRes drawableRes: Int,
                                @StringRes stringRes: Int,
                                @ColorRes backgroundColorRes: Int = R.color.mdu_grey_50,
                                @ColorRes drawableColorRes: Int = R.color.mdu_grey_700,
                                @ColorRes textColorRes: Int = R.color.mdu_text_black_secondary,
                                largeIcon: Boolean = false): View {
            val placeholderView = LayoutInflater.from(context).inflate(R.layout.placeholder, null)

            val background = placeholderView.findViewById(R.id.background)
            background.setBackgroundColor(ContextCompat.getColor(context, backgroundColorRes))

            val image = placeholderView.findViewById(R.id.imageView) as ImageView
            image.setImageDrawable(tintDrawable(context, drawableRes, drawableColorRes))
            if (largeIcon) {
                image.layoutParams.width = dpToPixels(context, 112)
                image.layoutParams.height = dpToPixels(context, 112)
            }

            val text = placeholderView.findViewById(R.id.textView) as TextView
            text.setText(stringRes)
            text.setTextColor(ContextCompat.getColor(context, textColorRes))

            return placeholderView
        }

        @JvmStatic
        fun dpToPixels(context: Context, dps: Int): Int {
            val density = context.resources.displayMetrics.density
            val dpAsPixels = (dps * density + 0.5f).toInt()
            return dpAsPixels
        }

    }
}
