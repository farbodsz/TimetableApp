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

import android.app.Activity
import android.app.TimePickerDialog
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.widget.TextView
import android.widget.TimePicker
import co.timetableapp.R
import co.timetableapp.util.DateUtils
import org.threeten.bp.LocalTime

/**
 * A helper class for setting up a [TextView] to show and change the time.
 */
class TimeSelectorHelper(private val activity: Activity, @IdRes private val textViewResId: Int) {

    private val mTextView = activity.findViewById(textViewResId) as TextView

    private var mTime: LocalTime? = null

    /**
     * Sets up this helper class, displaying the date and preparing actions for when the
     * [TextView] is clicked.
     *
     * @param initialTime   the time to initially display on the [TextView]. This can be null, in
     *                      which case a hint text will be initially displayed.
     * @param onTimeSet     a function to be invoked when the time has been changed
     */
    fun setup(initialTime: LocalTime?,
              onTimeSet: (view: TimePicker, time: LocalTime) -> Unit) {
        mTime = initialTime
        updateTimeText()

        setupOnClick(onTimeSet)
    }

    private fun setupOnClick(onTimeSet: (TimePicker, LocalTime) -> Unit) = mTextView.setOnClickListener {
        val listener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            onTimeSet.invoke(view, LocalTime.of(hourOfDay, minute))
        }

        val displayedTime = mTime ?: LocalTime.of(9, 0)

        TimePickerDialog(
                activity,
                listener,
                displayedTime.hour,
                displayedTime.minute,
                true
        ).show()
    }

    /**
     * Updates the displayed text according to the [time].
     *
     * @param time          used to update the displayed text. This can be null, in which case a
     *                      'hint' text is shown.
     * @param hintTextRes   the string resource used to display the hint text
     */
    @JvmOverloads
    fun updateTime(time: LocalTime?, @StringRes hintTextRes: Int = R.string.property_time) {
        mTime = time
        updateTimeText(hintTextRes)
    }

    private fun updateTimeText(@StringRes hintTextRes: Int = R.string.property_time) {
        val time = mTime

        if (time == null) {
            mTextView.text = activity.getString(hintTextRes)
            mTextView.setTextColor(ContextCompat.getColor(activity, R.color.mdu_text_black_secondary))
        } else {
            mTextView.text = time.format(DateUtils.FORMATTER_FULL_DATE)
            mTextView.setTextColor(ContextCompat.getColor(activity, R.color.mdu_text_black))
        }
    }

}
