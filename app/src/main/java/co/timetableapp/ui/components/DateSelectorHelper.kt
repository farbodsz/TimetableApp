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
import android.app.DatePickerDialog
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.widget.DatePicker
import android.widget.TextView
import co.timetableapp.R
import co.timetableapp.util.DateUtils
import org.threeten.bp.LocalDate

/**
 * A helper class for setting up a [TextView] to show and change the date.
 */
class DateSelectorHelper(val activity: Activity, @IdRes val textViewResId: Int) {

    private val mTextView = activity.findViewById(textViewResId) as TextView

    private var mDate: LocalDate? = null

    /**
     * Sets up this helper class, displaying the date and preparing actions for when the
     * [TextView] is clicked.
     *
     * @param initialDate   the date to initially display on the [TextView]. This can be null, in
     *                      which case a hint text will be initially displayed.
     * @param onDateSet     a function to be invoked when the date has been changed
     */
    fun setup(initialDate: LocalDate?,
              onDateSet: (view: DatePicker, date: LocalDate) -> Unit) {
        mDate = initialDate
        updateDateText()

        setupOnClick(onDateSet)
    }

    private fun setupOnClick(onDateSet: (DatePicker, LocalDate) -> Unit) = mTextView.setOnClickListener {
        // N.B. month-1 and month+1 in code because Android month values are from 0-11 (to
        // correspond with java.util.Calendar) but LocalDate month values are from 1-12.

        val listener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            onDateSet.invoke(view, LocalDate.of(year, month + 1, dayOfMonth))
        }

        val displayedDate = mDate ?: LocalDate.now()

        DatePickerDialog(
                activity,
                listener,
                displayedDate.year,
                displayedDate.monthValue - 1,
                displayedDate.dayOfMonth
        ).show()
    }

    /**
     * Updates the displayed text according to the [date].
     *
     * @param date          used to update the displayed text. This can be null, in which case a
     *                      'hint' text is shown.
     * @param hintTextRes   the string resource used to display the hint text
     */
    @JvmOverloads
    fun updateDate(date: LocalDate?, @StringRes hintTextRes: Int = R.string.property_date) {
        mDate = date
        updateDateText(hintTextRes)
    }

    private fun updateDateText(@StringRes hintTextRes: Int = R.string.property_date) {
        val date = mDate

        if (date == null) {
            mTextView.text = activity.getString(hintTextRes)
            mTextView.setTextColor(ContextCompat.getColor(activity, R.color.mdu_text_black_secondary))
        } else {
            mTextView.text = date.format(DateUtils.FORMATTER_FULL_DATE)
            mTextView.setTextColor(ContextCompat.getColor(activity, R.color.mdu_text_black))
        }
    }

}
