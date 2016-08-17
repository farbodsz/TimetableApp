package com.satsumasoftware.timetable

import android.app.Activity
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import com.satsumasoftware.timetable.framework.Color

fun setBarColors(color: Color, activity: Activity, vararg views: View) {
    for (view in views) {
        view.setBackgroundColor(ContextCompat.getColor(activity, color.getPrimaryColorResId(activity)))
    }
    if (Build.VERSION.SDK_INT >= 21) {
        activity.window.statusBarColor =
                ContextCompat.getColor(activity, color.getPrimaryDarkColorResId(activity))
    }
}
