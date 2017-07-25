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

package co.timetableapp.ui

import android.app.Dialog
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.widget.FrameLayout
import co.timetableapp.R
import co.timetableapp.model.agenda.AgendaType

/**
 * A bottom sheet dialog fragment for selecting what kind of item to create.
 */
class NewItemSelectorFragment : BottomSheetDialogFragment() {

    private var newItemAction: ((View, Dialog, AgendaType) -> Unit)? = null

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)

        val bottomSheet = View.inflate(context, R.layout.bottom_sheet_new_item, null)
        dialog!!.setContentView(bottomSheet)

        setClickListeners(dialog)

        val bottomSheetParent = bottomSheet.parent as View
        val behavior = (bottomSheetParent.layoutParams as CoordinatorLayout.LayoutParams).behavior

        if (behavior != null && behavior is BottomSheetBehavior) {
            behavior.setBottomSheetCallback(bottomSheetBehaviorCallback)
        }
    }

    private fun setClickListeners(dialog: Dialog) {
        dialog.findViewById<FrameLayout>(R.id.frameLayout_assignment).setOnClickListener {
            newItemAction?.invoke(it, dialog, AgendaType.ASSIGNMENT)
        }
        dialog.findViewById<FrameLayout>(R.id.frameLayout_exam).setOnClickListener {
            newItemAction?.invoke(it, dialog, AgendaType.EXAM)
        }
        dialog.findViewById<FrameLayout>(R.id.frameLayout_event).setOnClickListener {
            newItemAction?.invoke(it, dialog, AgendaType.EVENT)
        }
    }

    /**
     * Specifies the action to be invoked when a view has been clicked to create a new agenda item.
     */
    fun onCreateNewAgendaItem(action: (View, Dialog, AgendaType) -> Unit) {
        newItemAction = action
    }

}
