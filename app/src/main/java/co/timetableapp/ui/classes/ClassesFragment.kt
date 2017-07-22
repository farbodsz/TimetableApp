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

package co.timetableapp.ui.classes

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import co.timetableapp.R
import co.timetableapp.data.handler.ClassHandler
import co.timetableapp.model.Class
import co.timetableapp.ui.base.ItemDetailActivity
import co.timetableapp.ui.base.ItemListFragment
import co.timetableapp.util.UiUtils

/**
 * A fragment for displaying a list of classes to the user.
 *
 * @see Class
 * @see ClassesActivity
 * @see ClassDetailActivity
 * @see ClassEditActivity
 */
class ClassesFragment : ItemListFragment<Class>() {

    companion object {
        private const val REQUEST_CODE_CLASS_DETAIL = 1
    }

    private var mShowAll = false

    override fun instantiateDataHandler() = ClassHandler(activity)

    override fun setupLayout() {
        super.setupLayout()
        setupFab()
    }

    private fun setupFab() {
        activity.findViewById(R.id.fab).setOnClickListener {
            val intent = Intent(activity, ClassEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL)
        }
    }

    override fun setupAdapter(): RecyclerView.Adapter<*> {
        val adapter = ClassesAdapter(activity, mItems!!)
        adapter.onItemClick { view, position ->
            val intent = Intent(activity, ClassDetailActivity::class.java)
            intent.putExtra(ItemDetailActivity.EXTRA_ITEM, mItems!![position])

            var bundle: Bundle? = null
            if (UiUtils.isApi21()) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity,
                        view,
                        getString(R.string.transition_1))
                bundle = options.toBundle()
            }

            startActivityForResult(intent, REQUEST_CODE_CLASS_DETAIL, bundle)
        }

        return adapter
    }

    override fun fetchItems() =
            (mDataHandler as ClassHandler).getCurrentClasses(activity.application, mShowAll)

    override fun sortList() = mItems!!.sortWith(Class.NaturalSortComparator(activity))

    override fun getPlaceholderView() = UiUtils.makePlaceholderView(
            activity,
            R.drawable.ic_class_black_24dp,
            R.string.placeholder_classes,
            R.color.mdu_blue_400,
            R.color.mdu_white,
            R.color.mdu_white,
            true)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CLASS_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                updateList()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater!!.inflate(R.menu.menu_classes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilterDialog() {
        val multiChoiceListener = DialogInterface.OnMultiChoiceClickListener { _, which, isChecked ->
            when (which) {
                0 -> mShowAll = isChecked
                else -> throw UnsupportedOperationException("expected position: 0")
            }
        }

        AlertDialog.Builder(activity)
                .setTitle(R.string.action_filter)
                .setMultiChoiceItems(
                        R.array.filter_classes_options,
                        booleanArrayOf(mShowAll),
                        multiChoiceListener)
                .setPositiveButton(R.string.action_filter, { _, _ -> updateList() })
                .show()
    }

}
